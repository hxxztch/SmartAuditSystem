package com.shenji.modules.dashboard.service;

import com.shenji.modules.dashboard.vo.DashboardVO;
import java.util.List;
import java.util.Map;

public interface DashboardService {
    DashboardVO getOverview(Long userId, List<String> roles);
    List<Map<String,Object>> listProjects(Long userId, List<String> roles);
}
