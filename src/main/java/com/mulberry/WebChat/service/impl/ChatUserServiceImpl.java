package com.mulberry.WebChat.service.impl;

import com.mulberry.WebChat.dto.UserDetailDTO;
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
        return mapper.selectAllByName(username);
    }

    @Override
    public boolean updateUserInfo(UserDetailDTO updates) {
        String username = updates.getUsername();
        UserDetailDTO target = mapper.selectAllByName(username);

        Long id = target.getId();
        String nickname = selectNotNull(target.getNickname(), updates.getNickname());
        String gender = selectNotNull(target.getGender(), updates.getGender());
        LocalDate birth = selectNotNull(target.getBirth(), updates.getBirth());
        String region = selectNotNull(target.getRegion(), updates.getRegion());
        String biography = selectNotNull(target.getBiography(), updates.getBiography());
        String email = selectNotNull(target.getEmail(), updates.getEmail());

        return mapper.updateDetailById(id, nickname, gender, birth, region, biography, email) == 1;
    }

    private <T> T selectNotNull(T oldValue, T newValue) {
        if (newValue != null) {
            return newValue;
        }
        return oldValue;
    }
}
