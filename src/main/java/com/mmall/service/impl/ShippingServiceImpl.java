package com.mmall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.mmall.common.ServerResponse;
import com.mmall.dao.ShippingMapper;
import com.mmall.pojo.Shipping;
import com.mmall.service.IShippingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by SMY on 2017/12/10.
 */
@Service("iShippingService")
public class ShippingServiceImpl implements IShippingService {

    @Autowired
    private ShippingMapper shippingMapper;

    /**
     * 添加收货地址
     *
     * @param shipping
     * @return
     */
    public ServerResponse<Integer> addShipping(Shipping shipping) {
        int id = shippingMapper.insertSelective(shipping);
        if (id > 0) {
            return ServerResponse.createBySuccess("新建地址成功", id);
        }
        return ServerResponse.createByErrorMessage("新建地址失败");
    }

    /**
     * 删除收货地址
     *
     * @param shippingId
     * @return
     */
    public ServerResponse<String> delShipping(int shippingId) {
        int result = shippingMapper.deleteByPrimaryKey(shippingId);
        if (result > 0) {
            return ServerResponse.createBySuccessMessage("删除地址成功");
        }
        return ServerResponse.createByErrorMessage("删除地址失败");
    }

    /**
     * 更新收货地址
     *
     * @param shipping
     * @return
     */
    public ServerResponse<String> updateShippingInfo(Shipping shipping) {
        int result = shippingMapper.updateByPrimaryKeySelective(shipping);
        if (result > 0) {
            return ServerResponse.createBySuccessMessage("更新地址成功");
        }
        return ServerResponse.createByErrorMessage("更新地址失败");
    }

    /**
     * 查看收货地址
     *
     * @param shippingId
     * @return
     */
    public ServerResponse<Shipping> viewShipping(int shippingId) {
        Shipping shipping = shippingMapper.selectByPrimaryKey(shippingId);
        if (shipping != null) {
            return ServerResponse.createBySuccess(shipping);
        }
        return ServerResponse.createByErrorMessage("没有找到该收货地址");
    }

    /**
     * 分页查询收货地址
     *
     * @param userId
     * @param pageNum
     * @param pageSize
     * @return
     */
    @Override
    public ServerResponse<PageInfo> getList(int userId, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Shipping> shippingList = shippingMapper.selectListByUserId(userId);
        PageInfo pageInfo = new PageInfo(shippingList);
        return ServerResponse.createBySuccess(pageInfo);
    }

}
