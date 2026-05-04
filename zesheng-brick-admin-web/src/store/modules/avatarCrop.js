const state = () => ({
  /** 裁剪工作区 blob: Object URL，由独立裁剪页消费并在离开时 revoke */
  workspaceObjectUrl: "",
})

const mutations = {
  setWorkspaceUrl(state, url) {
    if (state.workspaceObjectUrl && state.workspaceObjectUrl !== url) {
      try {
        URL.revokeObjectURL(state.workspaceObjectUrl)
      } catch {
        // ignore
      }
    }
    state.workspaceObjectUrl = url || ""
  },
  clearWorkspace(state) {
    if (state.workspaceObjectUrl) {
      try {
        URL.revokeObjectURL(state.workspaceObjectUrl)
      } catch {
        // ignore
      }
    }
    state.workspaceObjectUrl = ""
  },
}

export default {
  state,
  mutations,
}
