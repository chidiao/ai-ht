import { normalizeRole } from './permissions'

export function canAccessRoute(route, role) {
  if (route.meta?.public) {
    return true
  }
  const allowedRoles = route.meta?.roles
  if (!allowedRoles?.length) {
    return true
  }
  return allowedRoles.includes(normalizeRole(role))
}
