import { defineStore } from 'pinia'
export const useAppStore = defineStore('app', {
  state: () => ({ sidebar: { opened: true }, device: 'desktop', visitedViews: [], cachedViews: [], mobileSidebar: false }),
  getters: {
    isMobile: state => state.device === 'mobile'
  },
  actions: {
    toggleSidebar() { this.sidebar.opened = !this.sidebar.opened },
    toggleMobileSidebar() { this.mobileSidebar = !this.mobileSidebar },
    closeMobileSidebar() { this.mobileSidebar = false },
    setDevice(w) { this.device = w < 768 ? 'mobile' : 'desktop'; if (w >= 768) { this.mobileSidebar = false; this.sidebar.opened = true } },
    addVisitedView(view) { if (!this.visitedViews.some(v=>v.path===view.path)) this.visitedViews.push(Object.assign({},view)) },
    delVisitedView(view) { const i=this.visitedViews.findIndex(v=>v.path===view.path); if(i>=0) this.visitedViews.splice(i,1) }
  }
})
