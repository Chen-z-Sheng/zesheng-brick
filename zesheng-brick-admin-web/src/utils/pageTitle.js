import { title } from '@/config'

/**
 * @author https://github.com/Chen-z-Sheng/zhesheng-brick-admin-web.git （不想保留author可删除）
 * @description 设置标题
 * @param pageTitle
 * @returns {string}
 */
export default function getPageTitle(pageTitle) {
  if (pageTitle) return `${pageTitle}-${title}`
  return `${title}`
}
