package com.github.son_daehyeon.domain.user.schema;

import com.github.son_daehyeon.common.database.BaseSchema;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class User extends BaseSchema {

    String name;
    String avatar;
}

