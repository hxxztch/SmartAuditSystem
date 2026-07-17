package com.shenji.modules.dashboard.service.impl;

import com.shenji.modules.dashboard.mapper.DashboardMapper;
import com.shenji.modules.dashboard.service.DashboardService;
import com.shenji.modules.dashboard.vo.DashboardVO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Resource
    private DashboardMapper mapper;

    @Override
    public DashboardVO getOverview(Long userId, List<String> roles) {
        DashboardVO vo = new DashboardVO();
        boolean isAdmin = roles.contains("audit_director") || roles.contains("school_leader");
        boolean isManager = roles.contains("audit_manager");
        boolean isAuditor = roles.contains("auditor");

        if (isAdmin) {
            vo.setTotalProjects(mapper.countTotal());
            vo.setOngoingProjects(mapper.countOngoing());
            vo.setCompletedProjects(mapper.countCompleted());
            vo.setTotalAmountInvolved(mapper.sumAmount() != null ? mapper.sumAmount().longValue() : 0L);
            vo.setProjectTypeDist(mapper.groupByType());
            vo.setProjectStatusDist(mapper.groupByStatus());
        } else if (isManager) {
            vo.setTotalProjects(mapper.countByLeader(userId));
            vo.setOngoingProjects(mapper.countOngoingByLeader(userId));
            vo.setCompletedProjects(mapper.countCompletedByLeader(userId));
            vo.setTotalAmountInvolved(mapper.sumAmountByLeader(userId) != null ? mapper.sumAmountByLeader(userId).longValue() : 0L);
            vo.setProjectTypeDist(mapper.groupByTypeLeader(userId));
        } else {
            vo.setTotalProjects(mapper.countByMember(userId));
            vo.setOngoingProjects(mapper.countOngoingByMember(userId));
            vo.setCompletedProjects(mapper.countCompletedByMember(userId));
            vo.setTotalAmountInvolved(mapper.sumAmountByMember(userId) != null ? mapper.sumAmountByMember(userId).longValue() : 0L);
            vo.setProjectTypeDist(mapper.groupByTypeMember(userId));
        }

        // Common mock data
        vo.setTotalIssues(856); vo.setRectifiedIssues(723); vo.setRectifyRate(84.5);
        vo.setRectifyDist(Arrays.asList(m("name","已整改","value",723),m("name","整改中","value",78)));
        return vo;
    }

    @Override
    public List<Map<String,Object>> listProjects(Long userId, List<String> roles) {
        if (roles.contains("audit_director") || roles.contains("school_leader")) {
            return mapper.listProjects();
        } else if (roles.contains("audit_manager")) {
            return mapper.listProjectsByLeader(userId);
        } else {
            return mapper.listProjectsByMember(userId);
        }
    }

    private Map<String,Object> m(String k1, Object v1, String k2, Object v2) {
        Map<String,Object> m = new LinkedHashMap<>(); m.put(k1,v1); m.put(k2,v2); return m;
    }
}
