#!/usr/bin/env bash
# 将 target 下 fat jar 拷入 docker/admin、docker/client，再以本目录为上下文构建
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PARENT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
cd "${SCRIPT_DIR}"

ADMIN_JAR_NAME="zesheng-admin-service-0.0.1-SNAPSHOT.jar"
CLIENT_JAR_NAME="zesheng-client-service-0.0.1-SNAPSHOT.jar"
ADMIN_SRC="${PARENT_ROOT}/zesheng-admin-service/target/${ADMIN_JAR_NAME}"
CLIENT_SRC="${PARENT_ROOT}/zesheng-client-service/target/${CLIENT_JAR_NAME}"

if [[ ! -f "${ADMIN_SRC}" ]]; then
    echo "缺少管理端 Jar：${ADMIN_SRC}（请先 Maven package）" >&2
    exit 1
fi
if [[ ! -f "${CLIENT_SRC}" ]]; then
    echo "缺少 C 端 Jar：${CLIENT_SRC}（请先 Maven package）" >&2
    exit 1
fi

mkdir -p "${SCRIPT_DIR}/admin" "${SCRIPT_DIR}/client"
cp -f "${ADMIN_SRC}" "${SCRIPT_DIR}/admin/${ADMIN_JAR_NAME}"
cp -f "${CLIENT_SRC}" "${SCRIPT_DIR}/client/${CLIENT_JAR_NAME}"

ADMIN_TAG="zesheng-admin:0.0.1-SNAPSHOT"
CLIENT_TAG="zesheng-client:0.0.1-SNAPSHOT"

# 可选：export ZESHENG_DOCKER_BASE_IMAGE=eclipse-temurin:21-jre-alpine 强制直连 Docker Hub
BUILD_ARGS=()
if [[ -n "${ZESHENG_DOCKER_BASE_IMAGE:-}" ]]; then
    BUILD_ARGS+=(--build-arg "BASE_IMAGE=${ZESHENG_DOCKER_BASE_IMAGE}")
fi

docker build "${BUILD_ARGS[@]}" -f "${SCRIPT_DIR}/admin/Dockerfile" -t "${ADMIN_TAG}" "${SCRIPT_DIR}"
docker build "${BUILD_ARGS[@]}" -f "${SCRIPT_DIR}/client/Dockerfile" -t "${CLIENT_TAG}" "${SCRIPT_DIR}"

OUT_DIR="${SCRIPT_DIR}/out"
mkdir -p "${OUT_DIR}"

docker save -o "${OUT_DIR}/zesheng-admin_0.0.1-SNAPSHOT.tar" "${ADMIN_TAG}"
docker save -o "${OUT_DIR}/zesheng-client_0.0.1-SNAPSHOT.tar" "${CLIENT_TAG}"
docker save -o "${OUT_DIR}/zesheng-brick-services_0.0.1-SNAPSHOT.tar" "${ADMIN_TAG}" "${CLIENT_TAG}"

echo "已生成："
echo "  ${OUT_DIR}/zesheng-admin_0.0.1-SNAPSHOT.tar"
echo "  ${OUT_DIR}/zesheng-client_0.0.1-SNAPSHOT.tar"
echo "  ${OUT_DIR}/zesheng-brick-services_0.0.1-SNAPSHOT.tar"
