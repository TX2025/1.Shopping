import request from '../utils/request'

export function getAdminProducts(params) { return request.get('/admin/products', { params }) }
export function createProduct(data) { return request.post('/admin/products', data) }
export function updateProduct(id, data) { return request.put(`/admin/products/${id}`, data) }
export function deleteProduct(id) { return request.delete(`/admin/products/${id}`) }
export function updateProductStatus(id, status) { return request.put(`/admin/products/${id}/status?status=${status}`) }

export function getAdminOrders(params) { return request.get('/admin/orders', { params }) }
export function updateOrderStatus(id, status) { return request.put(`/admin/orders/${id}/status?status=${status}`) }

export function getAdminUsers(params) { return request.get('/admin/users', { params }) }
export function updateUserStatus(id, status) { return request.put(`/admin/users/${id}/status?status=${status}`) }

export function getAdminCategories() { return request.get('/admin/categories') }
export function createCategory(data) { return request.post('/admin/categories', data) }
export function updateCategory(id, data) { return request.put(`/admin/categories/${id}`, data) }
export function deleteCategory(id) { return request.delete(`/admin/categories/${id}`) }

export function getSiteConfigs() { return request.get('/admin/site-configs') }
export function updateSiteConfigs(data) { return request.put('/admin/site-configs', data) }

export function getPageConfigs() { return request.get('/admin/page-configs') }
export function updatePageConfig(pageType, data) { return request.put(`/admin/page-configs/${pageType}`, data) }

export function getPageConfigSchema(pageType) { return request.get(`/admin/page-configs/${pageType}/schema`) }

export function getSiteConfig() { return request.get('/site-config') }
export function getPageConfig(type) { return request.get(`/page-config/${type}`) }
