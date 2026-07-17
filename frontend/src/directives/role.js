import { useUserStore } from '@/store/modules/user'
export default {
  mounted(el, binding) {
    const userStore = useUserStore()
    const roles = Array.isArray(binding.value) ? binding.value : [binding.value]
    if (!roles.some(r => userStore.roles.includes(r))) el.parentNode?.removeChild(el)
  }
}
