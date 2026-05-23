<template>
  <div class="checkout-page">
    <div class="container">
      <h2>确认订单</h2>
      <p v-if="pageConfig.instructionText" class="instruction">{{ pageConfig.instructionText }}</p>

      <el-card class="section">
        <template #header>收货地址</template>
        <div v-if="!addresses.length" style="padding:20px;text-align:center">
          <el-alert title="请先添加收货地址" type="warning" :closable="false" show-icon style="margin-bottom:12px" />
          <el-button type="primary" @click="showAddAddress = true">+ 新增收货地址</el-button>
        </div>
        <el-radio-group v-else v-model="selectedAddress">
          <el-radio v-for="addr in addresses" :key="addr.id" :value="addr.id" border style="margin: 0 12px 12px 0">
            {{ addr.receiverName }} {{ addr.receiverPhone }} {{ addr.province }}{{ addr.city }}{{ addr.district }} {{ addr.detail }}
          </el-radio>
        </el-radio-group>
        <el-button v-if="addresses.length" style="margin-top:8px" @click="showAddAddress = true">+ 新增地址</el-button>
      </el-card>

      <el-card class="section">
        <template #header>订单明细</template>
        <div v-for="item in cart.items" :key="item.id" class="order-item">
          <span>{{ item.productName }} x{{ item.quantity }}</span>
          <span>¥{{ (item.productPrice * item.quantity).toFixed(2) }}</span>
        </div>
        <div class="order-total">合计: ¥{{ cart.totalAmount }}</div>
      </el-card>

      <div class="remark">
        <el-input v-model="remark" placeholder="订单备注（选填）" />
      </div>

      <div class="submit-area">
        <el-button type="danger" size="large" @click="submitOrder" :disabled="!selectedAddress">提交订单</el-button>
      </div>
    </div>

    <el-dialog v-model="showAddAddress" title="新增地址" width="500px">
      <el-form :model="newAddress" label-width="80px">
        <el-form-item label="收货人"><el-input v-model="newAddress.receiverName" /></el-form-item>
        <el-form-item label="联系电话"><el-input v-model="newAddress.receiverPhone" /></el-form-item>
        <el-form-item label="省"><el-input v-model="newAddress.province" /></el-form-item>
        <el-form-item label="市"><el-input v-model="newAddress.city" /></el-form-item>
        <el-form-item label="区"><el-input v-model="newAddress.district" /></el-form-item>
        <el-form-item label="详细地址"><el-input v-model="newAddress.detail" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddAddress = false">取消</el-button>
        <el-button type="primary" @click="saveAddress">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '../../stores/cart'
import { getAddresses, addAddress } from '../../api/user'
import { createOrder } from '../../api/order'
import { getPageConfig } from '../../api/admin'
import { ElMessage } from 'element-plus'

const router = useRouter()
const cart = useCartStore()
const addresses = ref([])
const selectedAddress = ref(null)
const remark = ref('')
const showAddAddress = ref(false)
const pageConfig = ref({ instructionText: '请在15分钟内完成支付' })

const newAddress = ref({ receiverName: '', receiverPhone: '', province: '', city: '', district: '', detail: '' })

onMounted(async () => {
  await cart.fetchCart()
  if (!cart.items.length) { router.push('/cart'); return }
  try {
    const res = await getAddresses()
    addresses.value = res.data || []
    const def = addresses.value.find(a => a.isDefault)
    if (def) selectedAddress.value = def.id
    else if (addresses.value.length) selectedAddress.value = addresses.value[0].id
  } catch {}
  try {
    const pr = await getPageConfig('PAYMENT')
    if (pr.data?.configJson) pageConfig.value = { ...pageConfig.value, ...JSON.parse(pr.data.configJson) }
  } catch {}
})

async function saveAddress() {
  try {
    const addr = newAddress.value
    if (!addr.receiverName || !addr.receiverPhone || !addr.detail) {
      ElMessage.warning('请填写必填信息'); return
    }
    const res = await addAddress(addr)
    addresses.value.push(res.data)
    selectedAddress.value = res.data.id
    showAddAddress.value = false
    newAddress.value = { receiverName: '', receiverPhone: '', province: '', city: '', district: '', detail: '' }
  } catch {}
}

async function submitOrder() {
  try {
    const res = await createOrder({ addressId: selectedAddress.value, remark: remark.value })
    ElMessage.success('下单成功')
    router.push(`/thank-you?orderId=${res.data.id}&orderNo=${res.data.orderNo}`)
  } catch {}
}
</script>

<style scoped>
.container { max-width: 800px; margin: 30px auto; padding: 0 20px; }
.container h2 { margin-bottom: 8px; }
.instruction { color: #e6a23c; margin-bottom: 16px; background: #fdf6ec; padding: 8px 16px; border-radius: 4px; }
.section { margin-bottom: 16px; }
.order-item { display: flex; justify-content: space-between; padding: 8px 0; border-bottom: 1px solid #eee; }
.order-total { text-align: right; margin-top: 12px; font-size: 18px; color: #f56c6c; font-weight: bold; }
.remark { margin: 16px 0; }
.submit-area { text-align: right; }
</style>
