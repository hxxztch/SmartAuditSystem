package com.shenji.modules.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shenji.modules.system.domain.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    @Select("SELECT r.role_code FROM sys_role r INNER JOIN sys_user_role ur ON r.role_id = ur.role_id WHERE ur.user_id = #{userId} AND r.status = 1")
    List<String> selectRoleCodesByUserId(Long userId);

    @Select("SELECT DISTINCT m.perms FROM sys_menu m INNER JOIN sys_role_menu rm ON m.menu_id = rm.menu_id WHERE rm.role_id = #{roleId} AND m.perms IS NOT NULL AND m.perms != ''")
    List<String> selectPermsByRoleId(@Param("roleId") Long roleId);

    @Select("SELECT DISTINCT m.perms FROM sys_menu m INNER JOIN sys_role_menu rm ON m.menu_id = rm.menu_id INNER JOIN sys_user_role ur ON rm.role_id = ur.role_id WHERE ur.user_id = #{userId} AND m.perms IS NOT NULL AND m.perms != '' UNION SELECT target_id FROM temp_grant WHERE user_id = #{userId} AND type = 'DATA_PERM' AND status = 'ACTIVE'")
    List<String> selectPermsByUserId(Long userId);
}