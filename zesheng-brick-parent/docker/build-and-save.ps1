# UTF-8 BOM：避免 Windows PowerShell 5.x 按 ANSI 解码导致中文乱码
# 将 Maven target 下的 fat jar 拷入 docker/admin、docker/client，再以 docker/ 为上下文构建（上下文内只有 Dockerfile + jar，无 target 垃圾）
$ErrorActionPreference = "Stop"

$DockerRoot = Resolve-Path $PSScriptRoot
$ParentRoot = Resolve-Path (Join-Path $PSScriptRoot "..")

$AdminJarName = "zesheng-admin-service-0.0.1-SNAPSHOT.jar"
$ClientJarName = "zesheng-client-service-0.0.1-SNAPSHOT.jar"
$AdminJarSrc = Join-Path $ParentRoot "zesheng-admin-service\target\$AdminJarName"
$ClientJarSrc = Join-Path $ParentRoot "zesheng-client-service\target\$ClientJarName"

if (-not (Test-Path $AdminJarSrc)) {
    throw "缺少管理端 Jar：$AdminJarSrc （请先 Maven package）"
}
if (-not (Test-Path $ClientJarSrc)) {
    throw "缺少 C 端 Jar：$ClientJarSrc （请先 Maven package）"
}

$AdminJarDstDir = Join-Path $DockerRoot "admin"
$ClientJarDstDir = Join-Path $DockerRoot "client"
New-Item -ItemType Directory -Force -Path $AdminJarDstDir | Out-Null
New-Item -ItemType Directory -Force -Path $ClientJarDstDir | Out-Null
Copy-Item -LiteralPath $AdminJarSrc -Destination (Join-Path $AdminJarDstDir $AdminJarName) -Force
Copy-Item -LiteralPath $ClientJarSrc -Destination (Join-Path $ClientJarDstDir $ClientJarName) -Force

$AdminTag = "zesheng-admin:0.0.1-SNAPSHOT"
$ClientTag = "zesheng-client:0.0.1-SNAPSHOT"

# -f 相对「当前目录」解析，在 parent 下执行脚本时必须用绝对路径
$AdminDockerfile = Join-Path $DockerRoot "admin\Dockerfile"
$ClientDockerfile = Join-Path $DockerRoot "client\Dockerfile"

# 可选：$env:ZESHENG_DOCKER_BASE_IMAGE="eclipse-temurin:21-jre-alpine" 强制直连 Docker Hub
$BuildArgs = @()
if (-not [string]::IsNullOrWhiteSpace($env:ZESHENG_DOCKER_BASE_IMAGE)) {
    $BuildArgs += "--build-arg", "BASE_IMAGE=$($env:ZESHENG_DOCKER_BASE_IMAGE)"
}

docker build @BuildArgs -f $AdminDockerfile -t $AdminTag $DockerRoot
if ($LASTEXITCODE -ne 0) { throw "docker build 管理端镜像失败" }
docker build @BuildArgs -f $ClientDockerfile -t $ClientTag $DockerRoot
if ($LASTEXITCODE -ne 0) { throw "docker build C 端镜像失败" }

$OutDir = Join-Path $DockerRoot "out"
New-Item -ItemType Directory -Force -Path $OutDir | Out-Null

$AdminTar = Join-Path $OutDir "zesheng-admin_0.0.1-SNAPSHOT.tar"
$ClientTar = Join-Path $OutDir "zesheng-client_0.0.1-SNAPSHOT.tar"
$BundleTar = Join-Path $OutDir "zesheng-brick-services_0.0.1-SNAPSHOT.tar"

docker save -o $AdminTar $AdminTag
if ($LASTEXITCODE -ne 0) { throw "docker save 管理端 tar 失败" }
docker save -o $ClientTar $ClientTag
if ($LASTEXITCODE -ne 0) { throw "docker save C 端 tar 失败" }
docker save -o $BundleTar $AdminTag $ClientTag
if ($LASTEXITCODE -ne 0) { throw "docker save 合并 tar 失败" }

Write-Host "已生成："
Write-Host "  $AdminTar"
Write-Host "  $ClientTar"
Write-Host "  $BundleTar"
