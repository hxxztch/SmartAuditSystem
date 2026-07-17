 package com.shenji.modules.dashboard.vo;
 
 import java.util.List;
 import java.util.Map;
 
 /** 驾驶舱概览视图对象 */
 public class DashboardVO {
     // 数据看板指标
     private Integer totalProjects;
     private Integer totalProjectsThisYear;
     private Integer ongoingProjects;
     private Integer completedProjects;
     private Integer totalIssues;
     private Integer rectifiedIssues;
     private Double rectifyRate;
     private Long totalAmountInvolved;
     private Long rectifiedAmount;
     // 图表数据
     private List<Map<String, Object>> projectTypeDist;
     private List<Map<String, Object>> projectStatusDist;
     private List<Map<String, Object>> monthlyTrend;
     private List<Map<String, Object>> rectifyDist;
     // 预警
     private List<Map<String, Object>> warnings;
 
     // getters & setters
     public Integer getTotalProjects() { return totalProjects; }
     public void setTotalProjects(Integer totalProjects) { this.totalProjects = totalProjects; }
     public Integer getTotalProjectsThisYear() { return totalProjectsThisYear; }
     public void setTotalProjectsThisYear(Integer totalProjectsThisYear) { this.totalProjectsThisYear = totalProjectsThisYear; }
     public Integer getOngoingProjects() { return ongoingProjects; }
     public void setOngoingProjects(Integer ongoingProjects) { this.ongoingProjects = ongoingProjects; }
     public Integer getCompletedProjects() { return completedProjects; }
     public void setCompletedProjects(Integer completedProjects) { this.completedProjects = completedProjects; }
     public Integer getTotalIssues() { return totalIssues; }
     public void setTotalIssues(Integer totalIssues) { this.totalIssues = totalIssues; }
     public Integer getRectifiedIssues() { return rectifiedIssues; }
     public void setRectifiedIssues(Integer rectifiedIssues) { this.rectifiedIssues = rectifiedIssues; }
     public Double getRectifyRate() { return rectifyRate; }
     public void setRectifyRate(Double rectifyRate) { this.rectifyRate = rectifyRate; }
     public Long getTotalAmountInvolved() { return totalAmountInvolved; }
     public void setTotalAmountInvolved(Long totalAmountInvolved) { this.totalAmountInvolved = totalAmountInvolved; }
     public Long getRectifiedAmount() { return rectifiedAmount; }
     public void setRectifiedAmount(Long rectifiedAmount) { this.rectifiedAmount = rectifiedAmount; }
     public List<Map<String, Object>> getProjectTypeDist() { return projectTypeDist; }
     public void setProjectTypeDist(List<Map<String, Object>> projectTypeDist) { this.projectTypeDist = projectTypeDist; }
     public List<Map<String, Object>> getProjectStatusDist() { return projectStatusDist; }
     public void setProjectStatusDist(List<Map<String, Object>> projectStatusDist) { this.projectStatusDist = projectStatusDist; }
     public List<Map<String, Object>> getMonthlyTrend() { return monthlyTrend; }
     public void setMonthlyTrend(List<Map<String, Object>> monthlyTrend) { this.monthlyTrend = monthlyTrend; }
     public List<Map<String, Object>> getRectifyDist() { return rectifyDist; }
     public void setRectifyDist(List<Map<String, Object>> rectifyDist) { this.rectifyDist = rectifyDist; }
     public List<Map<String, Object>> getWarnings() { return warnings; }
     public void setWarnings(List<Map<String, Object>> warnings) { this.warnings = warnings; }
 }
