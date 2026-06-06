<template>
  <div class="checkout-page">
    <div class="container">
      <h2>确认订单</h2>
      <p v-if="pageConfig.instructionText" class="instruction">{{ pageConfig.instructionText }}</p>

      <!-- Shipping Address -->
      <el-card class="section">
        <template #header>收货地址</template>
        <div v-if="!addresses.length" style="padding:20px;text-align:center">
          <el-alert title="请先添加收货地址" type="warning" :closable="false" show-icon style="margin-bottom:12px" />
          <el-button type="primary" @click="showAddAddress = true">+ 新增收货地址</el-button>
        </div>
        <el-radio-group v-else v-model="selectedAddress">
          <el-radio v-for="addr in addresses" :key="addr.id" :value="addr.id" border style="margin:0 12px 12px 0">
            {{ addr.receiverName }} {{ addr.receiverPhone }} {{ addr.province }}{{ addr.city }}{{ addr.district }} {{ addr.detail }}
          </el-radio>
        </el-radio-group>
        <el-button v-if="addresses.length" style="margin-top:8px" @click="showAddAddress = true">+ 新增地址</el-button>
      </el-card>

      <!-- Order Items -->
      <el-card class="section">
        <template #header>订单明细</template>
        <div v-for="item in cart.items" :key="item.id" class="order-item">
          <span>{{ item.productName }} x{{ item.quantity }}</span>
          <span>¥{{ (item.productPrice * item.quantity).toFixed(2) }}</span>
        </div>
        <div class="order-total">合计: ¥{{ cart.totalAmount }}</div>
      </el-card>

      <!-- Shipping Method -->
      <el-card class="section" v-if="shippingMethods.length">
        <template #header>配送方式</template>
        <el-radio-group v-model="selectedShipping">
          <el-radio v-for="m in shippingMethods" :key="m.id" :value="m.id" border style="margin:0 12px 12px 0">
            <span style="font-weight:500">{{ m.name }}</span>
            <span style="color:#909399;font-size:12px;margin-left:8px">{{ m.region }}</span>
            <span style="color:#e67e22;font-weight:600;margin-left:8px">{{ Number(m.fee)===0?'免费':'¥'+Number(m.fee).toFixed(2) }}</span>
            <span style="color:#909399;font-size:11px;margin-left:4px" v-if="m.freeThreshold&&Number(m.freeThreshold)>0">满¥{{ Number(m.freeThreshold).toFixed(2) }}免运费</span>
          </el-radio>
        </el-radio-group>
      </el-card>

      <!-- Payment Method -->
      <el-card class="section">
        <template #header>支付方式</template>
        <el-radio-group v-model="selectedPayment">
          <el-radio value="wechat" border style="margin:0 12px 12px 0"><span style="color:#07c160;font-weight:600">微信支付</span></el-radio>
          <el-radio value="alipay" border style="margin:0 12px 12px 0"><span style="color:#1677ff;font-weight:600">支付宝</span></el-radio>
          <el-radio value="card" border style="margin:0 12px 12px 0"><span style="color:#e17055;font-weight:600">银行卡</span></el-radio>
        </el-radio-group>
      </el-card>

      <!-- Remark -->
      <div class="remark">
        <el-input v-model="remark" placeholder="订单备注（选填）" />
      </div>

      <div class="submit-area">
        <el-button type="danger" size="large" @click="submitOrder" :disabled="!selectedAddress">提交订单</el-button>
      </div>
    </div>

    <!-- Add Address Dialog -->
    <el-dialog v-model="showAddAddress" title="新增地址" width="500px">
      <el-form :model="newAddress" label-width="80px">
        <el-form-item label="收货人"><el-input v-model="newAddress.receiverName" /></el-form-item>
        <el-form-item label="联系电话"><el-input v-model="newAddress.receiverPhone" /></el-form-item>
        <el-form-item label="省"><el-input v-model="newAddress.province" /></el-form-item>
        <el-form-item label="市"><el-input v-model="newAddress.city" /></el-form-item>
        <el-form-item label="区"><el-input v-model="newAddress.district" /></el-form-item>
        <el-form-item label="详细地址"><el-input v-model="newAddress.detail" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="showAddAddress = false">取消</el-button><el-button type="primary" @click="saveAddress">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '../../stores/cart'
import { getAddresses, addAddress } from '../../api/user'
import { getPublicShippingMethods } from '../../api/admin'
import { createOrder } from '../../api/order'
import { ElMessage } from 'element-plus'

const cart = useCartStore()
const router = useRouter()
const addresses = ref([])
const selectedAddress = ref(null)
const selectedShipping = ref(null)
const selectedPayment = ref('wechat')
const shippingMethods = ref([])
const remark = ref('')
const showAddAddress = ref(false)
const newAddress = ref({ receiverName:'', receiverPhone:'', province:'', city:'', district:'', detail:'' })

const pageConfig = ref({ instructionText:'请确认订单信息后提交' })

onMounted(async ()=>{
  try{ const r=await getAddresses(); addresses.value=r.data||[]; if(addresses.value.length) selectedAddress.value=addresses.value[0].id }catch{}
  try{ const s=await getPublicShippingMethods(); shippingMethods.value=s.data||[]; if(shippingMethods.value.length) selectedShipping.value=shippingMethods.value[0].id }catch{}
})

async function saveAddress(){
  try{ await addAddress(newAddress.value); ElMessage.success('地址已保存'); showAddAddress.value=false
    const r=await getAddresses(); addresses.value=r.data||[]; selectedAddress.value=addresses.value[addresses.value.length-1]?.id }catch{}
}

async function submitOrder(){
  if(!selectedAddress.value){ ElMessage.warning('请选择收货地址'); return }
  try{
    const addr=addresses.value.find(a=>a.id===selectedAddress.value)
    const ship=shippingMethods.value.find(s=>s.id===selectedShipping.value)
    await createOrder({ items:cart.items.map(i=>({productId:i.productId,quantity:i.quantity})), addressId:selectedAddress.value,
      paymentMethod:selectedPayment.value, channel:'PC官网', remark:remark.value||undefined,
      shippingMethod:ship?.name, shippingFee:ship?Number(ship.fee):0 })
    ElMessage.success('订单提交成功'); cart.clearCart(); router.push('/thank-you')
  }catch{}
}
</script>

<style scoped>
.container{max-width:800px;margin:0 auto;padding:24px}
h2{font-size:22px;margin-bottom:12px}
.instruction{color:#909399;font-size:13px;margin-bottom:16px}
.section{margin-bottom:16px}
.order-item{display:flex;justify-content:space-between;padding:8px 0;border-bottom:1px solid #f0f0f0;font-size:14px}
.order-total{text-align:right;font-size:18px;font-weight:700;color:#e17055;padding-top:12px}
.remark{margin-bottom:20px}
.submit-area{text-align:right}
</style>
