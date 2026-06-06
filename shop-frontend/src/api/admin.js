import request from '../utils/request'

// 上传文件到服务器
export function uploadFile(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/admin/upload', formData)
}

export function getAdminProducts(params) { return request.get('/admin/products', { params }) }
export function createProduct(data) { return request.post('/admin/products', data) }
export function updateProduct(id, data) { return request.put(`/admin/products/${id}`, data) }
export function deleteProduct(id) { return request.delete(`/admin/products/${id}`) }
export function updateProductStatus(id, status) { return request.put(`/admin/products/${id}/status?status=${status}`) }

export function getAdminOrders(params) { return request.get('/admin/orders', { params }) }
export function updateOrderStatus(id, status) { return request.put(`/admin/orders/${id}/status?status=${status}`) }
export function getOrderStats() { return request.get('/admin/orders/stats') }

export function getAdminUsers(params) { return request.get('/admin/users', { params }) }
export function updateUserStatus(id, status) { return request.put(`/admin/users/${id}/status?status=${status}`) }
export function getUserStats() { return request.get('/admin/users/stats') }

export function getAdminCategories() { return request.get('/admin/categories') }
export function getCategoryProductCounts() { return request.get('/admin/categories/product-counts') }
export function createCategory(data) { return request.post('/admin/categories', data) }
export function updateCategory(id, data) { return request.put(`/admin/categories/${id}`, data) }
export function deleteCategory(id) { return request.delete(`/admin/categories/${id}`) }

export function getCategoryProducts(categoryId, params) {
  return request.get('/admin/products/by-category', { params: { categoryId, ...params, ...(params?.status ? { status: params.status } : {}) } })
}
export function batchMoveProducts(data) { return request.put('/admin/products/batch-move', data) }
export function batchDeleteProducts(ids) { return request.post('/admin/products/batch-delete', { ids }) }

export function getSiteConfigs() { return request.get('/admin/site-configs') }
export function updateSiteConfigs(data) { return request.put('/admin/site-configs', data) }

export function getPageConfigs() { return request.get('/admin/page-configs') }
export function updatePageConfig(pageType, data) { return request.put(`/admin/page-configs/${pageType}`, data) }

export function getPageConfigSchema(pageType) { return request.get(`/admin/page-configs/${pageType}/schema`) }

export function getDashboardStats() { return request.get('/admin/dashboard') }

// 配送服务
export function getShippingMethods() { return request.get('/admin/shipping') }
export function createShippingMethod(data) { return request.post('/admin/shipping', data) }
export function updateShippingMethod(id, data) { return request.put(`/admin/shipping/${id}`, data) }
export function deleteShippingMethod(id) { return request.delete(`/admin/shipping/${id}`) }
export function updateShippingStatus(id, status) { return request.put(`/admin/shipping/${id}/status?status=${status}`) }

// 售后服务
export function getAfterSales(params) { return request.get('/admin/aftersales', { params }) }
export function createAfterSale(data) { return request.post('/admin/aftersales', data) }
export function updateAfterSale(id, data) { return request.put(`/admin/aftersales/${id}`, data) }
export function deleteAfterSale(id) { return request.delete(`/admin/aftersales/${id}`) }
export function getAfterSaleStats() { return request.get('/admin/aftersales/stats') }

// 询盘管理
export function getInquiries(params) { return request.get('/admin/inquiries', { params }) }
export function createInquiry(data) { return request.post('/admin/inquiries', data) }
export function updateInquiry(id, data) { return request.put(`/admin/inquiries/${id}`, data) }
export function deleteInquiry(id) { return request.delete(`/admin/inquiries/${id}`) }
export function getInquiryStats() { return request.get('/admin/inquiries/stats') }

// 营销活动
export function getPromotions(status) { return request.get('/admin/promotions', { params: status ? { status } : {} }) }
export function createPromotion(data) { return request.post('/admin/promotions', data) }
export function updatePromotion(id, data) { return request.put(`/admin/promotions/${id}`, data) }
export function deletePromotion(id) { return request.delete(`/admin/promotions/${id}`) }
export function getPromotionStats() { return request.get('/admin/promotions/stats') }

export function getPublicShippingMethods() { return request.get('/shipping-methods') }

export function getSiteConfig() { return request.get('/site-config') }
export function getPageConfig(type) { return request.get(`/page-config/${type}`) }
