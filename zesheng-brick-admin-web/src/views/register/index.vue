<template>
  <div class="register-container">
    <div class="register-box">
      <div class="register-form-container">
        <div class="logo-container">
          <h2 class="welcome-text">暂不提供自助注册</h2>
          <p class="hint">
            管理端账号仅可由<strong>超级管理员</strong>在「权限管理 → 用户管理」中创建。
          </p>
        </div>
        <el-button class="back-btn" type="primary" @click="goLogin">
          返回登录
        </el-button>
      </div>
      <div class="register-image">
        <div class="overlay">
          <h2 class="slogan">开启您的管理之旅</h2>
          <p class="description">泽晟搬砖助手管理系统</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from "vue-router";

const router = useRouter();

const goLogin = () => {
  router.push("/login");
};
</script>

<style lang="scss" scoped>
.register-container {
  height: 100vh;
  width: 100vw;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

.register-box {
  width: 80%;
  max-width: 1000px;
  min-height: 420px;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  display: flex;
  background-color: #fff;
}

.register-form-container {
  width: 50%;
  padding: 50px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.logo-container {
  margin-bottom: 28px;
  text-align: center;

  .welcome-text {
    font-size: 26px;
    color: #333;
    margin-bottom: 16px;
    font-weight: 600;
  }

  .hint {
    font-size: 15px;
    color: #606266;
    line-height: 1.6;
    margin: 0;
  }
}

.back-btn {
  width: 100%;
  max-width: 280px;
  margin: 0 auto;
  height: 48px;
  border-radius: 24px;
}

.register-image {
  width: 50%;
  position: relative;
  background: url("~@/assets/login_images/background.jpg") center center no-repeat;
  background-size: cover;

  .overlay {
    position: absolute;
    inset: 0;
    background: rgba(0, 0, 0, 0.4);
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    padding: 40px;

    .slogan {
      color: #fff;
      font-size: 28px;
      font-weight: 600;
      margin-bottom: 16px;
      text-align: center;
    }

    .description {
      color: rgba(255, 255, 255, 0.9);
      font-size: 15px;
      text-align: center;
    }
  }
}

@media screen and (max-width: 992px) {
  .register-box {
    flex-direction: column;
    width: 100%;
    max-width: 100%;
  }

  .register-form-container,
  .register-image {
    width: 100%;
  }

  .register-image {
    height: 180px;
    order: -1;
  }
}
</style>
