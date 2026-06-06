<template>
  <div class="as-page">
    <div class="container">
      <h2>申请售后</h2>
      <el-card style="max-width:600px">
        <el-form :model="form" label-width="100px" label-position="top">
          <el-form-item label="关联订单号"><el-input v-model="form.orderNo" placeholder="如 #ORD-2025-001" /></el-form-item>
          <el-row :gutter="16">
            <el-col :span="12"><el-form-item label="联系人"><el-input v-model="form.customer" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="售后类型">
              <el-select v-model="form.type" style="width:100%">
                <el-option label="退货退款" value="RETURN_REFUND" /><el-option label="换货" value="EXCHANGE" />
                <el-option label="仅退款" value="REFUND_ONLY" /><el-option label="维修" value="REPAIR" />
              </el-select>
            </el-form-item></el-col>
          </el-row>
          <el-form-item label="退款金额"><el-input-number v-model="form.amount" :min="0" :precision="2" style="width:100%" placeholder="若无退款请填0" /></el-form-item>
          <el-form-item label="原因说明"><el-input v-model="form.reason" type="textarea" :rows="4" placeholder="请详细描述售后原因..." /></el-form-item>
          <el-form-item><el-button type="primary" @click="submit" :loading="saving">提交申请</el-button></el-form-item>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import request from '../../utils/request'
import { ElMessage } from 'element-plus'

const form=ref({orderNo:'',customer:'',type:'RETURN_REFUND',amount:0,reason:''})
const saving=ref(false)

async function submit(){
  if(!form.value.orderNo.trim()){ElMessage.warning('请输入关联订单号');return}
  if(!form.value.reason.trim()){ElMessage.warning('请填写原因说明');return}
  saving.value=true
  try{await request.post('/api/aftersales',form.value);ElMessage.success('售后申请已提交，请等待审核');form.value={orderNo:'',customer:'',type:'RETURN_REFUND',amount:0,reason:''}}catch{}finally{saving.value=false}
}
</script>

<style scoped>
.container{max-width:650px;margin:30px auto;padding:0 20px}
h2{margin-bottom:20px}
</style>
