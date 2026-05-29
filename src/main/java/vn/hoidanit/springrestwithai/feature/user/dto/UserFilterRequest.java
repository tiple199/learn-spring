package vn.hoidanit.springrestwithai.feature.user.dto;

import vn.hoidanit.springrestwithai.util.constant.GenderEnum;

public record UserFilterRequest(
        String name,
        String email,
        String address,
        Integer ageFrom,
        Integer ageTo,
        GenderEnum gender) {
}
