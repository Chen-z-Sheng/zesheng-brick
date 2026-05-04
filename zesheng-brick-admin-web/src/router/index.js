/**
 * @author https://github.com/Chen-z-Sheng/zhesheng-brick-admin-web.git （不想保留author可删除）
 * @description router全局配置，如有必要可分文件抽离，其中asyncRoutes只有在intelligence模式下才会用到，vip文档中已提供路由的基础图标与小清新图标的配置方案，请仔细阅读
 */

import { createRouter, createWebHashHistory } from "vue-router";
import Layout from "@/layouts/index.vue";
import EmptyLayout from "@/layouts/EmptyLayout.vue";
import { publicPath } from "@/config";

export const constantRoutes = [
  {
    path: "/login",
    component: () => import("@/views/login/index.vue"),
    hidden: true,
  },
  {
    path: "/register",
    component: () => import("@/views/register/index.vue"),
    hidden: true,
  },
  {
    path: "/401",
    name: "401",
    component: () => import("@/views/401.vue"),
    hidden: true,
  },
  {
    path: "/404",
    name: "404",
    component: () => import("@/views/404.vue"),
    hidden: true,
  },
  /** 头像裁剪：独立全屏页，对齐 cropper-next-vue 文档演示环境（无侧栏、无 el-dialog） */
  {
    path: "/avatar-crop",
    component: EmptyLayout,
    hidden: true,
    children: [
      {
        path: "",
        name: "AvatarCropStandalone",
        component: () => import("@/views/account/avatar-crop-standalone.vue"),
        meta: { title: "裁剪头像" },
      },
    ],
  },
];

export const asyncRoutes = [

  {
    path: "/",
    component: Layout,
    redirect: "/index",
    children: [
      {
        path: "index",
        name: "Index",
        component: () => import("@/views/index/index.vue"),
        meta: {
          title: "首页",
          icon: "home",
          affix: true,
        },
      },
      {
        path: "personal-center",
        name: "PersonalCenter",
        component: () => import("@/views/account/personal-center.vue"),
        hidden: true,
        meta: {
          title: "个人中心",
        },
      },
      {
        path: "preferences",
        name: "Preferences",
        component: () => import("@/views/account/preferences.vue"),
        hidden: true,
        meta: {
          title: "系统设置",
        },
      },
    ],
  },

  /* {
    path: "/test",
    component: Layout,
    redirect: "noRedirect",
    children: [
      {
        path: "test",
        name: "Test",
        component: () => import("@/views/test/index"),
        meta: {
          title: "test",
          icon: "marker",
          permissions: ["admin"],
        },
      },
    ],
  }, */

  {
    path: "/vab",
    component: Layout,
    redirect: "noRedirect",
    name: "Vab",
    alwaysShow: true,
    meta: { title: "组件", icon: "box-open", defaultOpen: true },
    children: [
      {
        path: "/form-schemes/edit",
        name: "FormSchemesEdit",
        hidden: true,
        meta: { title: "编辑方案" },
        component: () => import("@/views/form-schemes/designer.vue"),
      },
      // {
      //   path: "tree",
      //   name: "Tree",
      //   component: () => import("@/views/vab/tree.vue"),
      //   meta: {
      //     title: "树形控件",
      //     permissions: ["admin"],
      //   },
      // },
      // {
      //   path: "icon",
      //   name: "Icon",
      //   component: () => import("@/views/vab/icon.vue"),
      //   meta: {
      //     title: "图标",
      //     permissions: ["admin"],
      //   },
      // },
      {
        path: "form-schemes",
        name: "form-schemes",
        component: () => import("@/views/form-schemes/index.vue"),
        meta: {
          title: "方案管理",
          permissions: ["admin:form-scheme:list"],
        },
      },
      {
        path: "form-submissions",
        name: "form-submissions",
        component: () => import("@/views/form-submissions/index.vue"),
        meta: {
          title: "固结报单记录",
          permissions: ["admin:form-submission:list"],
        },
      },
      {
        path: "form-submissions/detail",
        name: "FormSubmissionDetail",
        hidden: true,
        meta: { title: "固结报单详情", activeMenu: "/vab/form-submissions" },
        component: () => import("@/views/form-submissions/detail.vue"),
      },
      {
        path: "sell-order-submissions",
        name: "sell-order-submissions",
        component: () => import("@/views/sell-order-submissions/index.vue"),
        meta: {
          title: "行情报单记录",
          permissions: ["admin:sell-order-submission:list"],
        },
      },
      {
        path: "sell-order-submissions/detail",
        name: "SellOrderSubmissionDetail",
        hidden: true,
        meta: { title: "行情报单详情", activeMenu: "/vab/sell-order-submissions" },
        component: () => import("@/views/sell-order-submissions/detail.vue"),
      },
      {
        path: "delivery-address",
        name: "delivery-address",
        component: () => import("@/views/delivery-address/index.vue"),
        meta: {
          title: "下单地址",
          permissions: ["admin:delivery-address:list"],
        },
      },
      {
        path: "logistics-company",
        name: "logistics-company",
        component: () => import("@/views/logistics-company/index.vue"),
        meta: {
          title: "物流公司管理",
          permissions: ["admin:logistics-company:list"],
        },
      },
      // {
      //   path: "chart",
      //   name: "Chart",
      //   component: () => import("@/views/vab/chart.vue"),
      //   meta: {
      //     title: "图表",
      //     permissions: ["admin"],
      //   },
      // },
      {
        path: "permissions",
        name: "Permissions",
        component: () => import("@/views/vab/permissions.vue"),
        meta: {
          title: "权限管理",
          permissions: ["sys:permission:list", "sys:role:list"],
        },
      },
      // {
      //   path: "nested",
      //   component: () => import("@/views/vab/nested.vue"),
      //   name: "Nested",
      //   redirect: "/vab/nested/menu1",
      //   meta: {
      //     title: "嵌套路由",
      //     permissions: ["admin"],
      //   },
      //   children: [
      //     {
      //       path: "menu1",
      //       component: () => import("@/views/vab/nested/menu1.vue"),
      //       name: "Menu1",
      //       redirect: "/vab/nested/menu1/menu2",
      //       meta: { title: "一级菜单" },
      //       children: [
      //         {
      //           path: "menu2",
      //           component: () => import("@/views/vab/nested/menu1/menu2.vue"),
      //           name: "Menu2",
      //           redirect: "/vab/nested/menu1/menu2/menu3",
      //           meta: { title: "二级菜单" },
      //           children: [
      //             {
      //               path: "menu3",
      //               component: () => import("@/views/vab/nested/menu1/menu2/menu3.vue"),
      //               name: "Menu3",
      //               meta: { title: "三级菜单" },
      //             },
      //           ],
      //         },
      //       ],
      //     },
      //   ],
      // },
      // {
      //   path: "editor",
      //   name: "Editor",
      //   component: () => import("@/views/vab/editor.vue"),
      //   meta: {
      //     title: "富文本编辑器",
      //     permissions: ["admin"],
      //   },
      // },
      // {
      //   path: "upload",
      //   name: "Upload",
      //   component: () => import("@/views/vab/upload.vue"),
      //   meta: {
      //     title: "文件上传",
      //     permissions: ["admin"],
      //   },
      // },
      {
        path: "settings",
        name: "Settings",
        component: () => import("@/views/vab/settings.vue"),
        meta: {
          title: "系统配置",
          permissions: ["admin:config:list"],
        },
      },
      // {
      //   path: "notification",
      //   name: "Notification",
      //   component: () => import("@/views/vab/notification.vue"),
      //   meta: {
      //     title: "通知中心",
      //     permissions: ["admin"],
      //   },
      // },
      // {
      //   path: "calendar",
      //   name: "Calendar",
      //   component: () => import("@/views/vab/calendar.vue"),
      //   meta: {
      //     title: "日历",
      //     permissions: ["admin"],
      //   },
      // },
      {
        path: "task",
        name: "Task",
        component: () => import("@/views/vab/task.vue"),
        meta: {
          title: "任务管理",
          permissions: ["sys:todo-task:list"],
        },
      },
      {
        path: "help-faq",
        name: "HelpFaq",
        component: () => import("@/views/vab/help-faq.vue"),
        meta: {
          title: "帮助FAQ管理",
          permissions: ["admin:help-faq:list"],
        },
      },
      {
        path: "user-feedback",
        name: "UserFeedback",
        component: () => import("@/views/vab/user-feedback.vue"),
        meta: {
          title: "问题反馈管理",
          permissions: ["admin:user-feedback:list"],
        },
      },
      {
        path: "announcements",
        name: "Announcements",
        component: () => import("@/views/vab/announcements.vue"),
        meta: {
          title: "公告管理",
          permissions: ["admin:announcement:list"],
        },
      },
      {
        path: "product",
        name: "Product",
        component: () => import("@/views/vab/product.vue"),
        meta: {
          title: "行情管理",
          permissions: ["admin:recycle-market:list"],
        },
      },
      // {
      //   path: "campaign",
      //   name: "Campaign",
      //   component: () => import("@/views/vab/campaign.vue"),
      //   meta: {
      //     title: "营销活动",
      //     permissions: ["admin"],
      //   },
      // },
    ],
  },
  {
    path: "/error",
    component: EmptyLayout,
    redirect: "noRedirect",
    name: "Error",
    meta: { title: "错误页", icon: "bug" },
    children: [
      {
        path: "401",
        name: "Error401",
        component: () => import("@/views/401"),
        meta: { title: "401" },
      },
      {
        path: "404",
        name: "Error404",
        component: () => import("@/views/404"),
        meta: { title: "404" },
      },
    ],
  },
  {
    path: "/:pathMatch(.*)*",
    redirect: "/404",
    hidden: true,
  },
];

const router = createRouter({
  history: createWebHashHistory(publicPath),
  routes: constantRoutes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition;
    } else {
      return { top: 0 };
    }
  },
});

export function resetRouter() {
  // 注意：所有动态路由路由必须带有name属性，否则可能会不能完全重置干净
  try {
    router.getRoutes().forEach((route) => {
      const { name } = route;
      if (name && name !== "Login") {
        router.hasRoute(name) && router.removeRoute(name);
      }
    });
  } catch (error) {
    // 强制刷新浏览器，不要用这种方式
    window.location.reload();
  }
}

export default router;
