# --- 第一阶段：构建阶段 (Node.js) ---
FROM node:18 AS build-stage

WORKDIR /app

# 先复制依赖描述文件，利用缓存
COPY package*.json ./

# 安装依赖 (使用淘宝镜像源，防止卡死)
RUN npm install --registry=https://registry.npmmirror.com

# 复制所有源代码
COPY . .

# 开始打包 (生成 dist 文件夹)
RUN npm run build

# --- 第二阶段：运行阶段 (Nginx) ---
FROM nginx:alpine AS production-stage

# 安装 curl 以便进行健康检查
RUN apk add --no-cache curl

# 把第一阶段生成的 dist 文件夹里的东西，拿过来
COPY --from=build-stage /app/dist /usr/share/nginx/html

# 复制 nginx 配置文件
COPY ./conf/nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]