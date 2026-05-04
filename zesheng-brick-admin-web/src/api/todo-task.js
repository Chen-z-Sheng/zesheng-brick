import request from '@/utils/request'

const BASE = '/admin/sys/todo-task'

export function getTodoTaskList(params) {
    return request.get(BASE, { params }).then((res) => res.data)
}

export function createTodoTask(data) {
    return request.post(BASE, data).then((res) => res.data)
}

export function updateTodoTask(id, data) {
    return request.patch(`${BASE}/${id}`, data).then((res) => res.data)
}

export function updateTodoTaskStatus(id, status) {
    return request.patch(`${BASE}/${id}/status`, null, { params: { status } }).then((res) => res.data)
}

export function deleteTodoTask(id) {
    return request.delete(`${BASE}/${id}`).then((res) => res.data)
}
