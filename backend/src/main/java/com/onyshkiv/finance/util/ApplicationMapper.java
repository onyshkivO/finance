package com.onyshkiv.finance.util;

import com.onyshkiv.finance.model.dto.request.SignUpRequest;
import com.onyshkiv.finance.model.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ApplicationMapper {
    User signUpRequestToUser(SignUpRequest signUpRequest);
}
