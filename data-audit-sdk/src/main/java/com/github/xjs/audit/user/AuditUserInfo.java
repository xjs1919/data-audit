/** 
 * copyright(c) 2019-2029 mamcharge.com
 */
 
package com.github.xjs.audit.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * user info
 *
 * @author 605162215@qq.com
 * @date 2019/12/4 16:51
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuditUserInfo {
    private String userId;
    private String userName;
    private String userNick;
}
