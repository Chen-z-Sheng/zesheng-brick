export function createEmptyExpressItem() {
  return { no: '' }
}

/** 将 expressNos / logisticsNos 转为编辑列表 */
export function normalizeExpressNoList(source) {
  let nos = []
  if (Array.isArray(source)) {
    nos = source
      .map((item) => (item && typeof item === 'object' ? item.no : item))
      .map((s) => String(s || '').trim())
      .filter(Boolean)
  } else if (source && typeof source === 'object') {
    const raw = source.expressNos || source.logisticsNos
    if (Array.isArray(raw)) {
      nos = raw.map((s) => String(s).trim()).filter(Boolean)
    }
  }
  return nos.length ? nos.map((no) => ({ no })) : [createEmptyExpressItem()]
}

export function collectExpressNos(list) {
  const seen = new Set()
  const out = []
  ;(list || []).forEach((item) => {
    const no = (item && item.no ? String(item.no) : '').trim()
    if (no && !seen.has(no)) {
      seen.add(no)
      out.push(no)
    }
  })
  return out
}

export function formatExpressNosDisplay(source) {
  let nos = []
  if (Array.isArray(source)) {
    nos = source.map((s) => String(s).trim()).filter(Boolean)
  } else if (source && typeof source === 'object') {
    const raw = source.expressNos || source.logisticsNos
    if (Array.isArray(raw)) {
      nos = raw.map((s) => String(s).trim()).filter(Boolean)
    }
  }
  const unique = [...new Set(nos)]
  return unique.length ? unique.join('、') : ''
}
