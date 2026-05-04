<template>
  <div class="permissions-container">
    <el-card shadow="never">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="角色权限" name="role">
          <el-table :data="roles" style="width: 100%" row-key="id">
            <el-table-column prop="name" label="角色名称" width="180" />
            <el-table-column label="权限">
              <template #default="{ row }">
                <el-tag
                  v-for="permission in row.permissions"
                  :key="permission"
                  style="margin-right: 10px"
                >
                  {{ permissionMap[permission] || permission }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180">
              <template #default="{ row }">
                <el-button v-permissions="['sys:role:update']" type="text" @click="editRole(row)">编辑</el-button>
                <el-button v-permissions="['sys:role:delete']" type="text" @click="deleteRole(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="用户管理" name="user">
          <div style="margin-bottom: 12px">
            <el-button v-permissions="['admin']" type="primary" @click="openCreateUser">
              新增用户
            </el-button>
          </div>
          <el-table :data="users" style="width: 100%" row-key="id">
            <el-table-column prop="name" label="用户名" width="180" />
            <el-table-column label="角色">
              <template #default="{ row }">
                <el-tag v-for="role in row.roles" :key="role" style="margin-right: 10px">
                  {{ roleMap[role] || role }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="phone" label="电话" width="200" />
            <el-table-column label="操作" width="180">
              <template #default="{ row }">
                <el-button v-permissions="['admin:user:update']" type="text" @click="editUser(row)">编辑</el-button>
                <el-button v-permissions="['admin:user:delete']" type="text" @click="deleteUser(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>

          <div style="margin-top: 16px; text-align: right">
            <el-pagination
              background
              layout="total, sizes, prev, pager, next, jumper"
              :current-page="userPage"
              :page-size="userPageSize"
              :page-sizes="[10, 20, 50, 100]"
              :total="userTotal"
              @size-change="handleUserSizeChange"
              @current-change="handleUserCurrentChange"
            />
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 角色编辑对话框 -->
    <el-dialog v-model="roleDialogVisible" title="编辑角色" width="500px">
      <el-form :model="currentRole" label-width="80px">
        <el-form-item label="角色名称">
          <el-input v-model="currentRole.name" />
        </el-form-item>
        <el-form-item label="权限">
          <el-checkbox-group v-model="currentRole.permissions">
            <el-checkbox
              v-for="perm in permissionOptions"
              :key="perm.code"
              :label="perm.code"
            >
              {{ perm.description || perm.code }}
            </el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="roleDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveRole">保存</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 超级管理员：新增用户 -->
    <el-dialog v-model="createUserDialogVisible" title="新增用户" width="520px" @closed="resetNewUser">
      <el-form ref="createUserForm" :model="newUser" :rules="createUserRules" label-width="88px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model.trim="newUser.username" maxlength="60" show-word-limit placeholder="登录用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="newUser.password" type="password" show-password placeholder="初始密码" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model.trim="newUser.phone" maxlength="11" placeholder="选填" />
        </el-form-item>
        <el-form-item label="角色" prop="roleId">
          <el-select v-model="newUser.roleId" placeholder="请选择角色" clearable filterable style="width: 100%">
            <el-option v-for="r in roles" :key="r.id" :label="r.name" :value="r.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="createUserDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitCreateUser">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 用户编辑对话框 -->
    <el-dialog v-model="userDialogVisible" title="编辑用户" width="500px">
      <el-form :model="currentUser" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="currentUser.name" />
        </el-form-item>
        <el-form-item label="电话">
          <el-input v-model="currentUser.phone" />
        </el-form-item>
        <el-form-item label="角色">
          <el-checkbox-group v-model="currentUser.roles">
            <el-checkbox v-for="(label, key) in roleMap" :key="key" :label="key">
              {{ label }}
            </el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="userDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveUser">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import {
  getRoleAll,
  updateRole,
  deleteRole,
  getPermissionAll,
  // getPermissionInfo,
} from "@/api/permission";
import {
  getAll as getUserAll,
  update as updateUser,
  deleteUser as deleteUserApi,
  createUser,
} from "@/api/user";
export default {
  name: "Permissions",
  data() {
    return {
      activeTab: "role",
      roleDialogVisible: false,
      userDialogVisible: false,
      createUserDialogVisible: false,
      newUser: {
        username: "",
        password: "",
        phone: "",
        roleId: null,
      },
      createUserRules: {
        username: [{ required: true, message: "请输入用户名", trigger: "blur" }],
        password: [
          { required: true, message: "请输入密码", trigger: "blur" },
          { min: 6, message: "密码至少6位", trigger: "blur" },
        ],
        roleId: [{ required: true, message: "请选择角色", trigger: "change" }],
      },
      currentRole: {
        id: "",
        name: "",
        permissions: [], // 存权限 code 数组
      },
      currentUser: {
        id: "",
        name: "",
        phone: "",
        roles: [],
      },
      roles: [],
      users: [],
      // 分页相关
      userPage: 1,
      userPageSize: 20,
      userTotal: 0,
      // 所有权限的选项列表
      permissionOptions: [],
      permissionMap: {},
      roleMap: {},
    };
  },
  created() {
    this.loadRoles();
    this.loadPermissions();
    this.loadUsers();
  },
  methods: {
    async loadRoles() {
      try {
        const res = await getRoleAll();
        const list = res.data || [];

        this.roles = list.map((role) => ({
          id: role.id,
          name: role.name,
          permissions: role.permissionCodes || [],
        }));

        this.roleMap = this.roles.reduce((map, role) => {
          map[role.id] = role.name;
          return map;
        }, {});
      } catch (e) {
        console.error(e);
        this.$message.error("加载角色列表失败");
      }
    },
    editRole(role) {
      this.currentRole = { ...role };
      this.roleDialogVisible = true;
    },

    async deleteRole(row) {
      try {
        await deleteRole(row.id);
        this.$message.success(`删除角色: ${row.name}`);
        this.loadRoles();
      } catch (e) {
        console.error(e);
        this.$message.error("删除角色失败");
      }
    },
    async saveRole() {
      try {
        await updateRole(this.currentRole.id, {
          name: this.currentRole.name,
          permissionCodes: this.currentRole.permissions,
        });
        this.$message.success("角色保存成功");
        this.roleDialogVisible = false;
        // 重新拉列表，拿最新数据
        this.loadRoles();
      } catch (e) {
        console.error(e);
        this.$message.error("角色保存失败");
      }
    },
    openCreateUser() {
      this.resetNewUser();
      this.createUserDialogVisible = true;
    },
    resetNewUser() {
      this.newUser = { username: "", password: "", phone: "", roleId: null };
      this.$nextTick(() => this.$refs.createUserForm?.clearValidate?.());
    },
    submitCreateUser() {
      this.$refs.createUserForm?.validate(async (valid) => {
        if (!valid) return;
        try {
          const payload = {
            username: this.newUser.username,
            password: this.newUser.password,
            roleId: this.newUser.roleId,
            status: 1,
          };
          if (this.newUser.phone) payload.phone = this.newUser.phone;
          await createUser(payload);
          this.$message.success("用户创建成功");
          this.createUserDialogVisible = false;
          this.loadUsers();
        } catch (e) {
          console.error(e);
          this.$message.error("创建用户失败");
        }
      });
    },
    editUser(user) {
      this.currentUser = {
        ...user,
        roles: (user.roles || []).map((r) => String(r)),
      };
      this.userDialogVisible = true;
    },
    async deleteUser(user) {
      try {
        await deleteUserApi(user.id);
        this.$message.success(`删除用户: ${user.name}`);
        this.loadUsers();
      } catch (e) {
        console.error(e);
        this.$message.error("删除用户失败");
      }
    },
    async saveUser() {
      try {
        const roleId = this.currentUser.roles && this.currentUser.roles[0];
        const payload = {
          username: this.currentUser.name,
          phone: this.currentUser.phone,
          roleId: roleId != null ? Number(roleId) : null,
        };

        await updateUser(this.currentUser.id, payload);

        this.$message.success("用户保存成功");
        this.userDialogVisible = false;
        this.loadUsers(); // 刷新列表
      } catch (e) {
        console.error(e);
        this.$message.error("用户保存失败");
      }
    },
    async loadPermissions() {
      try {
        const res = await getPermissionAll();
        const list = res.data || [];

        // 保存原始列表，给 checkbox 用
        this.permissionOptions = list;

        // 构建 code -> description 映射，给表格 tag 用
        this.permissionMap = list.reduce((map, perm) => {
          map[perm.code] = perm.description || perm.code;
          return map;
        }, {});
      } catch (e) {
        console.error(e);
        this.$message.error("加载权限列表失败");
      }
    },
    // 加载用户列表
    async loadUsers() {
      try {
        const res = await getUserAll({
          page: this.userPage,
          pageSize: this.userPageSize,
        });

        // 后端返回：{ code, msg, data: { pageMeta: {...}, records: [...] } }
        const pageData = res.data || {};
        const list = pageData.records || [];
        const meta = pageData.pageMeta || {};

        this.users = list.map((u) => ({
          id: u.id,
          name: u.username,
          phone: u.phone || u.remark || "",
          roles: u.roleId != null ? [String(u.roleId)] : [],
        }));

        this.userTotal = meta.total || 0;
        if (meta.page) this.userPage = meta.page;
        if (meta.pageSize) this.userPageSize = meta.pageSize;
      } catch (e) {
        console.error(e);
        this.$message.error("加载用户列表失败");
      }
    },
    // pageSize 改变
    handleUserSizeChange(size) {
      this.userPageSize = size;
      this.userPage = 1; // 一般改 pageSize 会重置到第 1 页
      this.loadUsers();
    },

    // 当前页改变
    handleUserCurrentChange(page) {
      this.userPage = page;
      this.loadUsers();
    },
  },
};
</script>

<style lang="scss" scoped>
.permissions-container {
  padding: 20px;

  .el-tag {
    margin-bottom: 5px;
  }
}
</style>
