# 🛠️ PREREQUISITES
- **Docker**
- **Docker Compose**
- **Git**
- **Linux | Ubuntu | WSL**

---

## 🚀 DOWNLOAD DOCKER & DOCKER COMPOSE

```bash
sudo apt-get update

```
```bash
sudo apt-get install ca-certificates curl
```
```
sudo install -m 0755 -d /etc/apt/keyrings
```
```
sudo curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
```
```
sudo chmod a+r /etc/apt/keyrings/docker.asc
```
```
echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu \
  $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | \
  sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
```
```
sudo apt-get update
```
```
sudo apt-get install docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
```


---
## ➡️ Run Docker Commands Without sudo

To run Docker commands without using sudo, add your user to the Docker group:

```bash
sudo usermod -aG docker $USER
```

 - After running this command, log out and log back in for the changes to take effect.
 - For Ubuntu users, Reboot/Restart device

---
## ✔️ Verify Installation

Check if Docker is installed and running by executing:

```bash
docker ps
```
- If you get permission denied, logout-login for WSL or Reboot for Ubuntu
- If you won't get any errors, that means it is installed succesfully


---
## 📦 Clone the Repository of Backend and Frontend

Clone the repository to your local machine:

```bash
git clone https://github.com/infracdo/hiveconnect_be.git
```
```
git clone https://github.com/infracdo/hiveconnect_fe.git
```
- note: clone it inside /home/user/

---
## 🏃‍♂️ Run Docker

Navigate to your project directory and start Docker:

```bash
cd /home/$USER/hiveconnect_be
```
```
docker compose up --build -d
```
if you have already used --build, use this for running wihout building image
```
docker compose up -d
```
to turn it off
```
docker compose down
```


---
## ✔️ Check Running Containers

Verify that your Docker services are running:

```bash
docker compose ps
```
Expected Result: A list of running services.


---
## 🌐 Access the Application

You can access the frontend and backend of your application locally:

  Frontend: [http://localhost:80](http://localhost:80/)
  
  Backend: [http://localhost:8080](http://localhost:8080/)

 Note: If the application is not accessible, ensure you have VPN access if required.
