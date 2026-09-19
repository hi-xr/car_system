/**
 * 车型相关 API
 * 对应数据库表 car1 (model_id, model_name, brand, guide_price, production_year, power_type, body_type)
 */

const BASE = '/api/car-models'

function unwrapResult(json) {
  // 后端统一 Result：{ success, code, msg, data }
  if (json && typeof json === 'object' && 'success' in json && 'data' in json) {
    return json.data
  }
  return json
}

/**
 * 获取全系车型列表
 * @returns {Promise<Array>} 车型列表
 */
export async function getCarModels() {
  const res = await fetch(BASE)
  if (!res.ok) throw new Error('车型列表加载失败')
  const raw = await res.json()
  const data = unwrapResult(raw)
  const list = data.models || data.carModels || data || []
  return Array.isArray(list) ? list : []
}

/**
 * 获取车型详情
 * @param {string|number} modelId 车型 ID
 * @returns {Promise<Object>} 车型详情
 */
export async function getCarModelDetail(modelId) {
  const res = await fetch(`${BASE}/${modelId}`)
  if (!res.ok) throw new Error('车型详情加载失败')
  const raw = await res.json()
  const data = unwrapResult(raw)
  return data.model || data
}
