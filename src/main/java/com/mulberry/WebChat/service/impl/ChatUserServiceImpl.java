package com.mulberry.WebChat.service.impl;

import com.mulberry.WebChat.dto.UserDetailDTO;
import com.mulberry.WebChat.exception.BusinessException;
import com.mulberry.WebChat.mapper.ChatUserMapper;
import com.mulberry.WebChat.service.ChatUserService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class ChatUserServiceImpl implements ChatUserService {
    private final ChatUserMapper mapper;

    public ChatUserServiceImpl(ChatUserMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public UserDetailDTO getUserInfo(UserDetails userDetail) {
        String username = userDetail.getUsername();
        UserDetailDTO userInfo = mapper.selectAllByName(username);
        if (userInfo == null) {
            throw new BusinessException("Select user info failed");
        }
        return userInfo;
    }

    @Override
    public void updateUserInfo(UserDetailDTO updates) {
        String username = updates.getUsername();
        UserDetailDTO target = mapper.selectAllByName(username);
        if (target == null) {
            throw new BusinessException("Username not exists");
        }

        Long id = target.getId();
        String nickname = selectNotNull(target.getNickname(), updates.getNickname());
        String gender = selectNotNull(target.getGender(), updates.getGender());
        LocalDate birth = selectNotNull(target.getBirth(), updates.getBirth());
        String region = selectNotNull(target.getRegion(), updates.getRegion());
        String biography = selectNotNull(target.getBiography(), updates.getBiography());
        String email = selectNotNull(target.getEmail(), updates.getEmail());

        if (mapper.updateDetailById(id, nickname, gender, birth, region, biography, email) != 1) {
            throw new BusinessException("Update infos failed");
        }
    }

    private <T> T selectNotNull(T oldValue, T newValue) {
        if (newValue != null) {
            return newValue;
        }
        return oldValue;
    }
}
