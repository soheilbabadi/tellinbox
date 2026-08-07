// package com.tellinbox.tellinbox_api.user.model;

// import com.tellinbox.tellinbox_api.user.enums.RoleName;
// import com.tellinbox.tellinbox_api.user.enums.UserStatus;
// import org.junit.jupiter.api.Test;

// import static org.junit.jupiter.api.Assertions.assertTrue;

// class UserModelTest {

//     @Test
//     void addRoleShouldLinkUserAndRoleBidirectionally() {
//         UserModel user = UserModel.builder()
//             .mobile("09123456789")
//             .fullName("Test User")
//             .status(UserStatus.ACTIVE)
//             .build();

//         RoleModel role = RoleModel.builder()
//             .name(RoleName.USER)
//             .description("Default role")
//             .build();

//         user.addRole(role);

//         assertTrue(user.getRoles().contains(role));
//         assertTrue(role.getUsers().contains(user));
//     }
// }
