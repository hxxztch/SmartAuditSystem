package com.shenji.modules.dashboard.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;
import java.util.Map;

@Mapper
public interface DashboardMapper {

    @Select("SELECT COUNT(*) FROM audit_project")
    int countTotal();

    @Select("SELECT COUNT(*) FROM audit_project WHERE status='ONGOING'")
    int countOngoing();

    @Select("SELECT COUNT(*) FROM audit_project WHERE status='COMPLETED'")
    int countCompleted();

    @Select("SELECT SUM(amount) FROM audit_project")
    Double sumAmount();

    @Select("SELECT type as name, COUNT(*) as value FROM audit_project GROUP BY type")
    List<Map<String,Object>> groupByType();

    @Select("SELECT status as name, COUNT(*) as value FROM audit_project GROUP BY status")
    List<Map<String,Object>> groupByStatus();

    @Select("SELECT code, name, type, org_name as orgName, leader_name as leader, start_date as startDate, end_date as endDate, status FROM audit_project")
    List<Map<String,Object>> listProjects();

    @Select("SELECT COUNT(*) FROM audit_project WHERE leader_id=#{userId}")
    int countByLeader(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM audit_project WHERE status='ONGOING' AND leader_id=#{userId}")
    int countOngoingByLeader(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM audit_project WHERE status='COMPLETED' AND leader_id=#{userId}")
    int countCompletedByLeader(@Param("userId") Long userId);

    @Select("SELECT SUM(amount) FROM audit_project WHERE leader_id=#{userId}")
    Double sumAmountByLeader(@Param("userId") Long userId);

    @Select("SELECT type as name, COUNT(*) as value FROM audit_project WHERE leader_id=#{userId} GROUP BY type")
    List<Map<String,Object>> groupByTypeLeader(@Param("userId") Long userId);

    @Select("SELECT code, name, type, org_name as orgName, leader_name as leader, start_date as startDate, end_date as endDate, status FROM audit_project WHERE leader_id=#{userId}")
    List<Map<String,Object>> listProjectsByLeader(@Param("userId") Long userId);

    @Select("SELECT COUNT(DISTINCT p.project_id) FROM audit_project p INNER JOIN audit_project_member m ON p.project_id=m.project_id WHERE m.user_id=#{userId}")
    int countByMember(@Param("userId") Long userId);

    @Select("SELECT COUNT(DISTINCT p.project_id) FROM audit_project p INNER JOIN audit_project_member m ON p.project_id=m.project_id WHERE m.user_id=#{userId} AND p.status='ONGOING'")
    int countOngoingByMember(@Param("userId") Long userId);

    @Select("SELECT COUNT(DISTINCT p.project_id) FROM audit_project p INNER JOIN audit_project_member m ON p.project_id=m.project_id WHERE m.user_id=#{userId} AND p.status='COMPLETED'")
    int countCompletedByMember(@Param("userId") Long userId);

    @Select("SELECT SUM(p.amount) FROM audit_project p INNER JOIN audit_project_member m ON p.project_id=m.project_id WHERE m.user_id=#{userId}")
    Double sumAmountByMember(@Param("userId") Long userId);

    @Select("SELECT p.type as name, COUNT(DISTINCT p.project_id) as value FROM audit_project p INNER JOIN audit_project_member m ON p.project_id=m.project_id WHERE m.user_id=#{userId} GROUP BY p.type")
    List<Map<String,Object>> groupByTypeMember(@Param("userId") Long userId);

    @Select("SELECT DISTINCT p.code, p.name, p.type, p.org_name as orgName, p.leader_name as leader, p.start_date as startDate, p.end_date as endDate, p.status FROM audit_project p INNER JOIN audit_project_member m ON p.project_id=m.project_id WHERE m.user_id=#{userId}")
    List<Map<String,Object>> listProjectsByMember(@Param("userId") Long userId);
}
