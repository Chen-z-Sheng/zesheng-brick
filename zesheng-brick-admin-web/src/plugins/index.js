/* 公共引入,勿随意修改,修改时需经过确认 */
import "./support";
import "@/styles/theme-palette.scss";
import "@/styles/spinner/dots.css";
import "@/styles/spinner/gauge.css";
import "@/styles/spinner/inner-circles.css";
import "@/styles/spinner/plus.css";
import "@/styles/vab.scss";
import "@/config/permission";
// 不再导入vab-icon
import VabPermissions from "layouts/Permissions";
import Vab from "@/utils/vab";
import ElementPlus from "element-plus";
import "element-plus/dist/index.css";
import { faToElIcon } from "@/utils/vab";
import { getElementIcon } from "@/utils/elementIcons";
import { h } from "vue";

// 创建全局VabIcon组件，用于替换之前的vab-icon
const VabIcon = {
  name: "VabIcon",
  props: {
    icon: {
      type: [String, Array],
      required: true,
    },
  },
  setup(props) {
    return () => {
      const iconName = faToElIcon(props.icon);
      const IconComponent = getElementIcon(iconName);
      return h("el-icon", {}, [h(IconComponent)]);
    };
  },
};

export default (app) => {
  app.use(ElementPlus);

  // 注册VabIcon组件，替代之前的vab-icon
  app.component("VabIcon", VabIcon);

  // 注册自定义插件
  app.use(Vab);
  app.use(VabPermissions);
};
