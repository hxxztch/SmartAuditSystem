 <template>
   <div class="error-page">
     <el-result icon="warning" title="404" sub-title="页面不存在">
       <template #extra>
         <el-button type="primary" @click="goBack">返回首页</el-button>
       </template>
     </el-result>
   </div>
 </template>
 
 <script setup>
 import { useRouter } from 'vue-router'
 const router = useRouter()
 function goBack() { router.push('/dashboard/overview') }
 </script>
 
 <style lang="scss" scoped>
 .error-page {
   display: flex;
   justify-content: center;
   align-items: center;
   min-height: 60vh;
 }
 </style>
