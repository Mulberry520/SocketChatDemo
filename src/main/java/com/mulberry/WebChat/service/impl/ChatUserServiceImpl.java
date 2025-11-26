package com.mulberry.WebChat.service.impl;

import com.mulberry.WebChat.common.CommonConst;
import com.mulberry.WebChat.dto.UserDetailDTO;
import com.mulberry.WebChat.exception.BusinessException;
import com.mulberry.WebChat.mapper.ChatUserMapper;
import com.mulberry.WebChat.service.ChatUserService;
import com.mulberry.WebChat.util.FileLoadUtil;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;

@Service
public class ChatUserServiceImpl implements ChatUserService {
    private final ChatUserMapper mapper;
    private final FileLoadUtil fileUtil;

    public ChatUserServiceImpl(
            ChatUserMapper mapper,
            FileLoadUtil fileUtil
    ) {
        this.mapper = mapper;
        this.fileUtil = fileUtil;
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

    @Override
    public String getUserAvatar(UserDetails userDetails) {
        String username = userDetails.getUsername();
        String avatar = mapper.selectAvatarByName(username);
        if (avatar != null) {
            return fileUtil.generateSignedUrl(avatar);
        }
        return null;
    }

    @Override
    public void updateUserAvatar(MultipartFile avatarFile, UserDetails userDetails) throws IOException {
        if (!FileLoadUtil.isImage(avatarFile)) {
            throw new IllegalArgumentException("Avatar should be an image file");
        }
        String username = userDetails.getUsername();
        String oldAvatar = mapper.selectAvatarByName(username);
        String newAvatar = fileUtil.ossSave(avatarFile, CommonConst.AVATAR_FOLDER_PREFIX);
        if (oldAvatar != null && fileUtil.isFileExists(oldAvatar)) {
            fileUtil.deleteFile(oldAvatar);
        }
        mapper.updateAvatarByName(username, newAvatar);
    }


    private <T> T selectNotNull(T oldValue, T newValue) {
        if (newValue != null) {
            return newValue;
        }
        return oldValue;
    }
}
