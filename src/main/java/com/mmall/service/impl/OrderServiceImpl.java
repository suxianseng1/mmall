package com.mmall.service.impl;

import com.alipay.api.AlipayResponse;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.demo.trade.config.Configs;
import com.alipay.demo.trade.model.ExtendParams;
import com.alipay.demo.trade.model.GoodsDetail;
import com.alipay.demo.trade.model.builder.AlipayTradePrecreateRequestBuilder;
import com.alipay.demo.trade.model.result.AlipayF2FPrecreateResult;
import com.alipay.demo.trade.service.AlipayTradeService;
import com.alipay.demo.trade.service.impl.AlipayTradeServiceImpl;
import com.alipay.demo.trade.utils.ZxingUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.dao.*;
import com.mmall.pojo.*;
import com.mmall.service.IOrderService;
import com.mmall.util.*;
import com.mmall.vo.OrderItemVo;
import com.mmall.vo.OrderProductVo;
import com.mmall.vo.OrderVo;
import com.mmall.vo.ShippingVo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

/**
 * Created by SMY on 2017/12/17.
 */
@Service("iOrderService")
public class OrderServiceImpl implements IOrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);
    // 支付宝当面付2.0服务
    private static AlipayTradeService tradeService;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private PayInfoMapper payInfoMapper;

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private ShippingMapper shippingMapper;

    static {
        /** 一定要在创建AlipayTradeService之前调用Configs.init()设置默认参数
         *  Configs会读取classpath下的zfbinfo.properties文件配置信息，如果找不到该文件则确认该文件是否在classpath目录
         */
        Configs.init("zfbinfo.properties");

        /** 使用Configs提供的默认参数
         *  AlipayTradeService可以使用单例或者为静态成员对象，不需要反复new
         */
        tradeService = new AlipayTradeServiceImpl.ClientBuilder().build();
    }

    /**
     * 插入一条订单，
     * 首先从购物车表中查询数据为 checked=1 的数据
     * 然后生成一个随机的订单号，设置 status 为未支付10 插入到Order表中，
     * 把订单详细信息再插入到Order_Item 表中
     * 删除Cart购物车表中 check =1 的数据
     * 减少Product中的库存
     * 最后返回 订单详细信息
     *
     * @param userId
     * @param shippingId
     * @return
     */
    @Override
    @Transactional
    public ServerResponse<OrderVo> createOrder(Integer userId, Integer shippingId) {
        List<Cart> cartList = cartMapper.selectByUserId(userId);
        if (cartList == null || cartList.size() == 0) {
            logger.debug("购物车为空");
            return ServerResponse.createByErrorMessage("购物车为空");
        }
        List<OrderItem> orderItemList = Lists.newArrayList();
        long orderNo = this.generatorOrderNo();
        List<Integer> productIds = Lists.newArrayList();
        for (Cart cart : cartList) {
            productIds.add(cart.getProductId());
            OrderItem orderItem = this.assembleOrderItem(cart, userId, orderNo);
            orderItemList.add(orderItem);
        }
        int result = orderItemMapper.insertBatch(orderItemList);
        if (result <= 0) {
            logger.error("插入订单详情失败");
            return ServerResponse.createByErrorMessage("插入订单详情失败");
        }
        Order order = this.getOrder(userId, shippingId, this.getTotalPrice(cartList));
        order.setOrderNo(orderNo);
        result = orderMapper.insert(order);
        if (result <= 0) {
            logger.error("插入订单失败");
            return ServerResponse.createByErrorMessage("插入订单失败");
        }
        result = cartMapper.deleteBatch(productIds,userId);
        if (result <= 0) {
            logger.error("清理购物车失败");
            return ServerResponse.createByErrorMessage("清理购物车失败");
        }
        // 减少库存
        this.subProductCount(orderItemList);
        List<OrderItemVo> orderItemVoList = this.parseOrderItemVo(orderItemList);
        OrderVo orderVo = this.parseOrderVo(order, orderItemVoList);
        logger.debug("添加订单成功");
        return ServerResponse.createBySuccess(orderVo);
    }

    /**
     * 获取订单详情
     * @param userId 用户ID
     * @param orderNo 订单号
     * @return
     */
    @Override
    public ServerResponse<OrderProductVo> getOrderCartProduct(Integer userId,Long orderNo) {
        List<OrderItem> orderItemList = orderItemMapper.selectListByUserIdAndOrderNo(orderNo,userId);
        if(orderItemList == null || orderItemList.size() == 0){
            logger.info("该订单没有商品{}",orderNo);
            return ServerResponse.createByErrorMessage("该订单没有商品");
        }
        List<OrderItemVo> orderItemVoList = this.parseOrderItemVo(orderItemList);
        OrderProductVo orderProductVo = new OrderProductVo();
        orderProductVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        orderProductVo.setOrderItemVoList(orderItemVoList);
        Order order = orderMapper.selectOrderByOrderNo(orderNo);
        orderProductVo.setProductTotalPrice(order.getPayment());
        return ServerResponse.createBySuccess(orderProductVo);
    }

    /**
     * 获取订单集合
     * @param pageSize 显示条数
     * @param pageNum 页码
     * @param userId 用户ID
     * @return
     */
    @Override
    public ServerResponse<PageInfo> getList(Integer pageSize, Integer pageNum, Integer userId) {
        PageHelper.startPage(pageNum,pageSize);
        List<Order> orderList = orderMapper.selectOrderByUserId(userId);
        PageInfo pageInfo = new PageInfo(orderList);
        List<OrderVo> orderVoList = parseOrderVoList(orderList,userId);
        pageInfo.setList(orderVoList);
        return ServerResponse.createBySuccess(pageInfo);
    }

    /**
     *  获取订单
     * @param orderNo 订单号
     * @param userId 用户ID
     * @return
     */
    @Override
    public ServerResponse<OrderVo> getDetail(Long orderNo, Integer userId) {
        List<OrderItem> orderItemList = orderItemMapper.selectListByUserIdAndOrderNo(orderNo,userId);
        List<OrderItemVo> orderItemVoList = this.parseOrderItemVo(orderItemList);
        Order order= orderMapper.selectOrderByOrderNo(orderNo);
        OrderVo orderVo = this.parseOrderVo(order,orderItemVoList);
        return ServerResponse.createBySuccess(orderVo);
    }

    /**
     *  取消订单
     * @param orderNo
     * @return
     */
    @Override
    @Transactional
    public ServerResponse cancle(Long orderNo) {
        int code = Const.OrderStatusEnum.CANCLEED.getCode();
        Order order = orderMapper.selectOrderByOrderNo(orderNo);
        order.setStatus(code);
        int result = orderMapper.updateByPrimaryKeySelective(order);
        if(result <= 0){
            return ServerResponse.createByErrorMessage("取消订单失败");
        }
        return ServerResponse.createBySuccess();
    }

    /**
     * 管理员 获取所有订单
     * @param pageSize
     * @param pageNum
     * @return
     */
    @Override
    public ServerResponse<PageInfo> manageList(Integer pageSize, Integer pageNum) {
        PageHelper.startPage(pageNum,pageSize);
        List<Order> orderList = orderMapper.selectAll();
        PageInfo pageInfo = new PageInfo(orderList);
        List<OrderVo> orderVoList = parseOrderVoList(orderList,null);
        pageInfo.setList(orderVoList);
        return ServerResponse.createBySuccess(pageInfo);
    }

    /**
     * 管理员 根据订单号获得订单信息
     * @param orderNo
     * @return
     */
    @Override
    public ServerResponse<OrderVo> searchByOrderNo(Long orderNo) {
        List<OrderItem> orderItemList = orderItemMapper.selectListByUserIdAndOrderNo(orderNo,null);
        List<OrderItemVo> orderItemVoList = this.parseOrderItemVo(orderItemList);
        Order order= orderMapper.selectOrderByOrderNo(orderNo);
        OrderVo orderVo = this.parseOrderVo(order,orderItemVoList);
        return ServerResponse.createBySuccess(orderVo);
    }

    /**
     * 订单发货
     * 设置 订单状态为 发货状态
     * @param orderNo
     * @return
     */
    @Override
    @Transactional
    public ServerResponse sendGoods(Long orderNo) {
        Order order = orderMapper.selectOrderByOrderNo(orderNo);
        order.setStatus(Const.OrderStatusEnum.SHIPPED.getCode());
        order.setSendTime(new Date());
        int result = orderMapper.updateByPrimaryKeySelective(order);
        if(result <= 0){
            return ServerResponse.createByErrorMessage("设置发货失败");
        }
        return ServerResponse.createBySuccess("设置发货成功");
    }


    /**
     * 获得OrderVoList， 如果传入userId则是普通用户，否则是管理员
     * @param orderList
     * @param userId
     * @return List<OrderVo>
     */
    private List<OrderVo> parseOrderVoList(List<Order> orderList,Integer userId){
        List<OrderVo> orderVoList = Lists.newArrayList();
        for(Order order : orderList){
            long orderNo = order.getOrderNo();
            List<OrderItem> orderItemList = Lists.newArrayList();
            if(userId == null ){
                orderItemList = orderItemMapper.selectListByUserIdAndOrderNo(orderNo,null);
            } else {
                orderItemList = orderItemMapper.selectListByUserIdAndOrderNo(orderNo,userId);
            }
            List<OrderItemVo> orderItemVoList = this.parseOrderItemVo(orderItemList);
            OrderVo orderVo = this.parseOrderVo(order,orderItemVoList);
            orderVoList.add(orderVo);
        }
        return orderVoList;
    }

    /**
     *  转换成OrderItem 对象
     * @param cart
     * @param userId
     * @param orderNo
     * @return OrderItem
     */
    private OrderItem assembleOrderItem(Cart cart, Integer userId, long orderNo) {
        BigDecimal tempPrice = new BigDecimal("0");
        Product product = productMapper.selectByPrimaryKey(cart.getProductId());
        tempPrice = BigDecimalUtil.mul(product.getPrice().doubleValue(), cart.getQuantity());
        OrderItem orderItem = new OrderItem();
        orderItem.setProductName(product.getName());
        orderItem.setProductId(product.getId());
        orderItem.setProductImage(product.getMainImage());
        orderItem.setQuantity(cart.getQuantity());
        orderItem.setUserId(userId);
        orderItem.setTotalPrice(tempPrice);
        orderItem.setCurrentUnitPrice(product.getPrice());
        orderItem.setOrderNo(orderNo);
        return orderItem;
    }

    /**
     * 计算订单总价
     */
    private BigDecimal getTotalPrice(List<Cart> cartList) {
        BigDecimal payment = new BigDecimal("0");
        for (Cart cart : cartList) {
            BigDecimal tempPrice = new BigDecimal("0");
            Product product = productMapper.selectByPrimaryKey(cart.getProductId());
            tempPrice = BigDecimalUtil.mul(product.getPrice().doubleValue(), cart.getQuantity());
            payment = BigDecimalUtil.add(payment.doubleValue(), tempPrice.doubleValue());
        }
        return payment;
    }


    /**
     * 减少库存
     *
     * @param orderItems
     */
    private void subProductCount(List<OrderItem> orderItems) {
        for (OrderItem orderItem : orderItems) {
            int num = orderItem.getQuantity();
            int currentNum = 0;
            Product product = productMapper.selectByPrimaryKey(orderItem.getProductId());
            currentNum = product.getStock() - num;
            product.setStock(currentNum);
            productMapper.updateByPrimaryKey(product);
        }
    }

    /**
     *  转换成OrderItemVO 集合对象
     * @param orderItems
     * @return List<OrderItemVo>
     */
    private List<OrderItemVo> parseOrderItemVo(List<OrderItem> orderItems) {
        List<OrderItemVo> orderItemVoList = Lists.newArrayList();
        for (OrderItem orderItem : orderItems) {
            OrderItemVo orderItemVo = new OrderItemVo();
            orderItemVo.setCreateTime(DateTimeUtil.dateToStr(orderItem.getCreateTime()));
            orderItemVo.setCurrentUnitPrice(orderItem.getCurrentUnitPrice());
            orderItemVo.setOrderNo(orderItem.getOrderNo());
            orderItemVo.setProductId(orderItem.getProductId());
            orderItemVo.setProductImage(orderItem.getProductImage());
            orderItemVo.setProductName(orderItem.getProductName());
            orderItemVo.setQuantity(orderItem.getQuantity());
            orderItemVo.setTotalPrice(orderItem.getTotalPrice());
            orderItemVoList.add(orderItemVo);
        }
        return orderItemVoList;
    }

    /**
     * 转换成OrderVo对象
     * @param order
     * @param orderItemVos
     * @return OrderVo
     */
    private OrderVo parseOrderVo(Order order, List<OrderItemVo> orderItemVos) {
        OrderVo orderVo = new OrderVo();
        orderVo.setCreateTime(DateTimeUtil.dateToStr(order.getCreateTime()));
        orderVo.setCloseTime(DateTimeUtil.dateToStr(order.getCloseTime()));
        orderVo.setEndTime(DateTimeUtil.dateToStr(order.getSendTime()));
        orderVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        orderVo.setOrderItemVoList(orderItemVos);
        orderVo.setPaymentType(order.getPaymentType());
        orderVo.setPayment(order.getPayment());
        orderVo.setPaymentTime(DateTimeUtil.dateToStr(order.getPaymentTime()));
        orderVo.setOrderNo(order.getOrderNo());
        orderVo.setPaymentTypeDesc(Const.PaymentTypeEnum.codeOf(order.getPaymentType()));

        Shipping shipping = shippingMapper.selectByPrimaryKey(order.getShippingId());
        if(shipping != null ) {
            orderVo.setShippingVo(this.assembleShipping(shipping));
        }
        orderVo.setShippingId(shipping.getId());
        orderVo.setStatus(order.getStatus());
        orderVo.setStatusDesc(Const.OrderStatusEnum.code(order.getStatus()));
        orderVo.setPostage(order.getPostage());
        return orderVo;
    }


    /**
     * 将Shipping  转换成ShippingVo对象
     * @param shipping
     * @return ShippingVo
     */
    private ShippingVo assembleShipping(Shipping shipping) {
        ShippingVo shippingVo = new ShippingVo();
        shippingVo.setReceiverAddress(shipping.getReceiverAddress());
        shippingVo.setReceiverCity(shipping.getReceiverCity());
        shippingVo.setReceiverDistrict(shipping.getReceiverDistrict());
        shippingVo.setReceiverMobile(shipping.getReceiverMobile());
        shippingVo.setReceiverName(shipping.getReceiverName());
        shippingVo.setReceiverPhone(shipping.getReceiverPhone());
        shippingVo.setReceiverProvince(shipping.getReceiverProvince());
        shippingVo.setReceiverZip(shipping.getReceiverZip());
        return shippingVo;
    }

    /**
     *  创建Order对象
     * @param userId
     * @param shippingId
     * @param payment
     * @return Order
     */
    private Order getOrder(int userId, Integer shippingId, BigDecimal payment) {
        Order order = new Order();
        order.setStatus(Const.OrderStatusEnum.NO_PAY.getCode());
        order.setPayment(payment);
        order.setPaymentType(Const.PaymentTypeEnum.ONLINE_PAY.getCode());
        order.setShippingId(shippingId);
        order.setPostage(0);
        order.setUserId(userId);
        return order;
    }

    /**
     * 获取随机订单号
     * @return
     */
    private long generatorOrderNo() {
        long curr = System.currentTimeMillis();
        return curr + new Random().nextInt(100);
    }


    /**
     * 支付宝预创建支付订单
     * @param userId
     * @param orderNo
     * @param path
     * @return
     */
    @Transactional
    public ServerResponse pay(Integer userId, Long orderNo, String path) {
        Map<String, String> mapResult = Maps.newHashMap();
        Order order = orderMapper.selectOrderByOrderNo(orderNo);
        if (order == null) {
            return ServerResponse.createBySuccessMessage("该订单不存在");
        }
        // (必填) 商户网站订单系统中唯一订单号，64个字符以内，只能包含字母、数字、下划线，
        // 需保证商户系统端不能重复，建议通过数据库sequence生成，
        String outTradeNo = order.getOrderNo().toString();

        // (必填) 订单标题，粗略描述用户的支付目的。如“xxx品牌xxx门店当面付扫码消费”
        String subject = "Happy_mmall 扫码付款";

        // (必填) 订单总金额，单位为元，不能超过1亿元
        // 如果同时传入了【打折金额】,【不可打折金额】,【订单总金额】三者,则必须满足如下条件:【订单总金额】=【打折金额】+【不可打折金额】
        String totalAmount = order.getPayment().toString();

        // (可选) 订单不可打折金额，可以配合商家平台配置折扣活动，如果酒水不参与打折，则将对应金额填写至此字段
        // 如果该值未传入,但传入了【订单总金额】,【打折金额】,则该值默认为【订单总金额】-【打折金额】
        String undiscountableAmount = "0";

        // 卖家支付宝账号ID，用于支持一个签约账号下支持打款到不同的收款账号，(打款到sellerId对应的支付宝账号)
        // 如果该字段为空，则默认为与支付宝签约的商户的PID，也就是appid对应的PID
        String sellerId = "";

        // 订单描述，可以对交易或商品进行一个详细地描述，比如填写"购买商品2件共15.00元"
        String body = new StringBuffer().append("订单:").append(order.getOrderNo()).append(",共花费").append(order.getPayment()).append("元").toString();

        // 商户操作员编号，添加此参数可以为商户操作员做销售统计
        String operatorId = "test_operator_id";

        // (必填) 商户门店编号，通过门店号和商家后台可以配置精准到门店的折扣信息，详询支付宝技术支持
        String storeId = "test_store_id";

        // 业务扩展参数，目前可添加由支付宝分配的系统商编号(通过setSysServiceProviderId方法)，详情请咨询支付宝技术支持
        ExtendParams extendParams = new ExtendParams();
        extendParams.setSysServiceProviderId("2088100200300400500");

        // 支付超时，定义为120分钟
        String timeoutExpress = "120m";

        // 商品明细列表，需填写购买商品详细信息，
        List<GoodsDetail> goodsDetailList = new ArrayList<GoodsDetail>();

        List<OrderItem> orderItemList = orderItemMapper.selectListByUserIdAndOrderNo(orderNo, userId);
        for (OrderItem orderItem : orderItemList) {
            GoodsDetail goods1 = GoodsDetail.newInstance(orderItem.getProductId().toString(), orderItem.getProductName(),
                    BigDecimalUtil.mul(orderItem.getCurrentUnitPrice().doubleValue(), new Double(100)).longValue(),
                    orderItem.getQuantity());
            goodsDetailList.add(goods1);
        }

        // 创建扫码支付请求builder，设置请求参数
        AlipayTradePrecreateRequestBuilder builder = new AlipayTradePrecreateRequestBuilder()
                .setSubject(subject).setTotalAmount(totalAmount).setOutTradeNo(outTradeNo)
                .setUndiscountableAmount(undiscountableAmount).setSellerId(sellerId).setBody(body)
                .setOperatorId(operatorId).setStoreId(storeId).setExtendParams(extendParams)
                .setTimeoutExpress(timeoutExpress)
                .setNotifyUrl(PropertiesUtil.getProperty("alipay.callback.url"))//支付宝服务器主动通知商户服务器里指定的页面http路径,根据需要设置
                .setGoodsDetailList(goodsDetailList);

        AlipayF2FPrecreateResult result = tradeService.tradePrecreate(builder);
        switch (result.getTradeStatus()) {
            case SUCCESS:
                logger.info("支付宝预下单成功: )");
                AlipayTradePrecreateResponse response = result.getResponse();
                dumpResponse(response);
                File folder = new File(path);
                if (!folder.exists()) {
                    folder.setWritable(true);
                    folder.mkdirs();
                }
                // 需要修改为运行机器上的路径
                String filePath = String.format(path + "/qr-%s.png",
                        response.getOutTradeNo());
                String fileName = String.format("/qr-%s.png", response.getOutTradeNo());

                logger.info("filePath:" + filePath);
                ZxingUtils.getQRCodeImge(response.getQrCode(), 256, filePath);
                File targetFile = new File(path, fileName);
                try {
                    FTPUtil.upload(Lists.newArrayList(targetFile));
                } catch (IOException e) {
                    logger.error("上传二维码失败", e);
                    e.printStackTrace();
                }
                // 刚刚上传到FTP的图片地址URL
                String qrPathUrl = PathUtil.getFTPImgPath(targetFile.getName());
                mapResult.put("qrPath", qrPathUrl);
                mapResult.put("orderNo", orderNo.toString());
                return ServerResponse.createBySuccess(mapResult);
            case FAILED:
                logger.error("支付宝预下单失败!!!");
                return ServerResponse.createByErrorMessage("支付宝预下单失败!!!");

            case UNKNOWN:
                logger.error("系统异常，预下单状态未知!!!");
                return ServerResponse.createByErrorMessage("系统异常，预下单状态未知!!!");

            default:
                logger.error("不支持的交易状态，交易返回异常!!!");
                return ServerResponse.createByErrorMessage("不支持的交易状态，交易返回异常!!!");
        }
    }

    /**
     * 支付宝回调函数，用来确定用户有没有支付成功
     * @param params
     * @return
     */
    @Override
    @Transactional
    public ServerResponse alipayCallBack(Map<String, String> params) {
        Long orderNo = Long.valueOf(params.get("out_trade_no"));
        String tradeNo = params.get("trade_no");
        String tradeStatus = params.get("trade_status");
        Order order = orderMapper.selectOrderByOrderNo(orderNo);
        if (order == null) {
            return ServerResponse.createByErrorMessage("该订单不存在");
        }
        if (order.getStatus() >= Const.OrderStatusEnum.PAID.getCode()) {
            return ServerResponse.createByErrorMessage("该订单已经完成，重复回调");
        }
        if (tradeStatus.equals(Const.AlipayCallback.TRADE_STATUS_TRADE_SUCCESS)) {
            order.setStatus(Const.OrderStatusEnum.PAID.getCode());
            order.setPaymentTime(new Date());
            int result = orderMapper.updateByPrimaryKeySelective(order);
            if (result <= 0) {
                return ServerResponse.createByErrorMessage("该订单已支付成功，订单更新状态失败");
            }
        }
        PayInfo payInfo = new PayInfo();
        payInfo.setOrderNo(order.getOrderNo());
        payInfo.setPayPlatform(Const.PayPlatformEnum.ALIPAY.getCode());
        payInfo.setPlatformStatus(tradeStatus);
        payInfo.setUserId(order.getUserId());
        payInfo.setPlatformNumber(tradeNo);
        int result = payInfoMapper.insert(payInfo);
        if (result <= 0) {
            return ServerResponse.createByErrorMessage("该订单已支付完成，添加PayInfo失败");
        }
        return ServerResponse.createBySuccess();
    }

    /**
     * 查询 订单状态
     * @param userId 用户ID
     * @param orderNo 订单号
     * @return
     */
    @Override
    public ServerResponse queryOrderPayStatus(Integer userId, Long orderNo) {
        Order order = orderMapper.selectOrderByOrderNo(orderNo);
        if (order == null) {
            return ServerResponse.createByErrorMessage("该用户并没有该订单,查询无效");
        }
        return ServerResponse.createBySuccess(order.getStatus() >= Const.OrderStatusEnum.PAID.getCode() ? Const.AlipayCallback.RESPONSE_SUCCESS : Const.AlipayCallback.RESPONSE_FAILED);
    }


    private void dumpResponse(AlipayResponse response) {
        if (response != null) {
            logger.info(String.format("code:%s, msg:%s", response.getCode(), response.getMsg()));
            if (StringUtils.isNotEmpty(response.getSubCode())) {
                logger.info(String.format("subCode:%s, subMsg:%s", response.getSubCode(),
                        response.getSubMsg()));
            }
            logger.info("body:" + response.getBody());
        }
    }
}
