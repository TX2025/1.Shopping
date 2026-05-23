package com.shop.service;

import com.shop.common.PageResult;
import com.shop.entity.User;
import com.shop.entity.UserAddress;
import java.util.List;

public interface UserService {
    User getProfile(Long userId);
    User updateProfile(Long userId, User user);
    List<UserAddress> getAddresses(Long userId);
    UserAddress addAddress(Long userId, UserAddress address);
    PageResult<User> adminList(int page, int size);
    void updateUserStatus(Long userId, String status);
}
