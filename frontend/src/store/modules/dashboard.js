import { defineStore } from 'pinia'
export const useDashboardStore = defineStore('dashboard', {
  state: () => ({ overviewData: null, drillLevel: 0, loading: false }),
  actions: { async fetchOverview() { this.overviewData = null } }
})
