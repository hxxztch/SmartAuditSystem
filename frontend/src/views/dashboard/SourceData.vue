<template>
  <DashboardLayout>
    <div class="source-page">
      <el-card shadow="never">
        <template #header>
          <span>原始明细数据</span>
          <el-tag type="warning" size="small" effect="plain" style="float:right">仅展示已授权范围内的明细数据</el-tag>
        </template>
        <p style="font-size:13px;color:#909399">底层原始数据，包含财务凭证、合同清单、支付单据、审计底稿等源头数据。</p>
      </el-card>
      <el-card shadow="never">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="财务凭证" name="voucher">
            <DetailTable :columns="voucherColumns" :data="voucherData" show-pagination :total="voucherData.length" />
          </el-tab-pane>
          <el-tab-pane label="合同清单" name="contract">
            <DetailTable :columns="contractColumns" :data="contractData" show-pagination :total="contractData.length" />
          </el-tab-pane>
          <el-tab-pane label="支付单据" name="payment">
            <DetailTable :columns="paymentColumns" :data="paymentData" show-pagination :total="paymentData.length" />
          </el-tab-pane>
          <el-tab-pane label="审计底稿" name="paper">
            <DetailTable :columns="paperColumns" :data="paperData" show-pagination :total="paperData.length" />
          </el-tab-pane>
        </el-tabs>
      </el-card>
    </div>
  </DashboardLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import DashboardLayout from '@/layout/DashboardLayout.vue'
import DetailTable from '@/components/dashboard/DetailTable.vue'

const activeTab = ref('voucher')
const sourceProjects = ref([])
const loading = ref(false)

const voucherColumns = [
  { prop:'date',label:'凭证日期' },{ prop:'code',label:'凭证号' },
  { prop:'summary',label:'摘要',minWidth:200 },{ prop:'deptAmount',label:'借方金额' },
  { prop:'credAmount',label:'贷方金额' },{ prop:'subject',label:'科目' }
]
const voucherData = ref([
  { date:'2025-01-15',code:'记-001',summary:'支付办公设备采购款',deptAmount:'35,000',credAmount:'35,000',subject:'固定资产' },
  { date:'2025-01-20',code:'记-015',summary:'拨付科研项目经费',deptAmount:'200,000',credAmount:'200,000',subject:'科研经费' },
  { date:'2025-02-10',code:'记-032',summary:'报销差旅费',deptAmount:'8,500',credAmount:'8,500',subject:'管理费用' }
])

const contractColumns = [
  { prop:'code',label:'合同编号' },{ prop:'name',label:'合同名称',minWidth:200 },
  { prop:'amount',label:'合同金额' },{ prop:'vendor',label:'供应商' },
  { prop:'signDate',label:'签订日期' },{ prop:'status',label:'状态' }
]
const contractData = ref([
  { code:'HT-2025-008',name:'服务器采购合同',amount:'1,280,000',vendor:'华为技术',signDate:'2025-03-01',status:'执行中' },
  { code:'HT-2025-023',name:'实验室改造工程合同',amount:'860,000',vendor:'中建三局',signDate:'2025-04-15',status:'已完成' }
])

const paymentColumns = [
  { prop:'date',label:'支付日期' },{ prop:'code',label:'支付单号' },
  { prop:'payee',label:'收款方',minWidth:180 },{ prop:'amount',label:'金额' },
  { prop:'method',label:'方式' },{ prop:'status',label:'状态' }
]
const paymentData = ref([
  { date:'2025-03-15',code:'ZF-001',payee:'华为技术',amount:'640,000',method:'转账',status:'已支付' },
  { date:'2025-04-20',code:'ZF-002',payee:'中建三局',amount:'430,000',method:'转账',status:'已支付' }
])

const paperColumns = [
  { prop:'code',label:'底稿编号' },{ prop:'title',label:'底稿名称',minWidth:200 },
  { prop:'author',label:'编制人' },{ prop:'date',label:'编制日期' },{ prop:'reviewStatus',label:'复核状态' }
]


const paperData = ref([
  { code:'DG-001',title:'采购业务审计工作底稿',author:'李四',date:'2026-02-15',reviewStatus:'已复核' },
  { code:'DG-002',title:'预算执行审计工作底稿',author:'王五',date:'2026-02-20',reviewStatus:'待复核' }
])
onMounted(async () => {
  try {
    const res = await request({url:'/dashboard/source-data',method:'get'})
    if (!res || !res.length) return
    voucherData.value = res.map(p => ({date:p.startdate,code:p.code,summary:p.name,deptAmount:((p.amount||0)/2).toLocaleString(),credAmount:((p.amount||0)/2).toLocaleString(),subject:p.type}))
    contractData.value = res.map(p => ({code:'HT-'+p.code,name:p.name,amount:((p.amount||0)).toLocaleString(),vendor:p.orgname,signDate:p.startdate,status:p.status}))
    paymentData.value = res.map(p => ({date:p.enddate||p.startdate,code:'ZF-'+p.code,payee:p.leader,amount:((p.amount||0)/2).toLocaleString(),method:'转账',status:p.status}))
    paperData.value = res.map(p => ({code:'DG-'+p.code,title:p.name,author:p.leader,date:p.startdate,reviewStatus:p.status==='COMPLETED'?'已复核':'待复核'}))
  } catch(e) {}
})
</script>

<style lang="scss" scoped>
.source-page{display:flex;flex-direction:column;gap:16px}
</style>


