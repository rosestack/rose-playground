#!/bin/bash

# User Modulith 服务启动脚本

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 打印带颜色的消息
print_message() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_header() {
    echo -e "${BLUE}================================${NC}"
    echo -e "${BLUE}  User Modulith Docker Services${NC}"
    echo -e "${BLUE}================================${NC}"
}

# 检查 Docker 是否安装
check_docker() {
    if ! command -v docker &> /dev/null; then
        print_error "Docker 未安装，请先安装 Docker"
        exit 1
    fi
    
    if ! command -v docker-compose &> /dev/null; then
        print_error "Docker Compose 未安装，请先安装 Docker Compose"
        exit 1
    fi
}

# 检查端口是否被占用
check_ports() {
    local ports=(3306 6379 8081 8082)
    local occupied_ports=()
    
    for port in "${ports[@]}"; do
        if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1; then
            occupied_ports+=($port)
        fi
    done
    
    if [ ${#occupied_ports[@]} -gt 0 ]; then
        print_warning "以下端口被占用: ${occupied_ports[*]}"
        print_warning "请确保这些端口可用，或修改 docker-compose.yml 中的端口映射"
        read -p "是否继续？(y/N): " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            exit 1
        fi
    fi
}

# 启动服务
start_services() {
    local compose_file="docker-compose.yml"
    
    # 检查是否只启动 MySQL
    if [[ "$1" == "mysql" ]]; then
        compose_file="docker-compose-mysql.yml"
        print_message "启动 MySQL 服务..."
    else
        print_message "启动所有服务 (MySQL + Redis + 管理工具)..."
    fi
    
    # 拉取最新镜像
    print_message "拉取 Docker 镜像..."
    docker-compose -f $compose_file pull
    
    # 启动服务
    print_message "启动服务..."
    docker-compose -f $compose_file up -d
    
    # 等待服务启动
    print_message "等待服务启动..."
    sleep 10
    
    # 检查服务状态
    print_message "检查服务状态..."
    docker-compose -f $compose_file ps
}

# 显示连接信息
show_connection_info() {
    echo
    print_header
    echo -e "${GREEN}服务已启动成功！${NC}"
    echo
    echo -e "${BLUE}MySQL 连接信息：${NC}"
    echo "  主机: localhost"
    echo "  端口: 3306"
    echo "  数据库: user_modulith"
    echo "  用户名: root"
    echo "  密码: password"
    echo
    
    if [[ "$1" != "mysql" ]]; then
        echo -e "${BLUE}Redis 连接信息：${NC}"
        echo "  主机: localhost"
        echo "  端口: 6379"
        echo "  密码: 无"
        echo
    fi
    
    echo -e "${BLUE}管理工具：${NC}"
    echo "  phpMyAdmin: http://localhost:8081"
    
    if [[ "$1" != "mysql" ]]; then
        echo "  Redis Commander: http://localhost:8082"
    fi
    
    echo
    echo -e "${BLUE}常用命令：${NC}"
    echo "  查看日志: docker-compose logs -f"
    echo "  停止服务: docker-compose down"
    echo "  重启服务: docker-compose restart"
    echo
}

# 主函数
main() {
    print_header
    
    # 检查依赖
    check_docker
    
    # 检查端口
    check_ports
    
    # 启动服务
    start_services "$1"
    
    # 显示连接信息
    show_connection_info "$1"
}

# 显示帮助信息
show_help() {
    echo "用法: $0 [选项]"
    echo
    echo "选项:"
    echo "  mysql    只启动 MySQL 和 phpMyAdmin"
    echo "  help     显示此帮助信息"
    echo "  (无参数)  启动所有服务"
    echo
    echo "示例:"
    echo "  $0           # 启动所有服务"
    echo "  $0 mysql     # 只启动 MySQL"
    echo "  $0 help      # 显示帮助"
}

# 解析命令行参数
case "${1:-}" in
    help|--help|-h)
        show_help
        exit 0
        ;;
    mysql)
        main "mysql"
        ;;
    "")
        main
        ;;
    *)
        print_error "未知选项: $1"
        show_help
        exit 1
        ;;
esac