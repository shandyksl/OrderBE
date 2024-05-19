# OrderBE

## Summary
The Order System is designed to efficiently manage orders, ensure inventory integrity, and provide seamless transaction experiences for users.

## Function
The following are the main functions of the backend API
1. 下单 PlaceOrder
2. 订单支付 OrderPayment
3. 获取订单详情 GetOrderDetails


## Technical
The project uses Java 18, Spring Boot 3, and incorporates Object-Oriented Programming (OOP), Dependency Injection, and Aspect-Oriented Programming (AOP).
It is recommended to use Intellij Idea Community for development, and to install its suggested extensions for auto-completion.

## Preparation

###### 1. Install IDE ，Clone this repository to your local machine.
- Install Intellij Idea Community [Intellij Idea Community](https://www.jetbrains.com/idea/download/?section=windows)(We are currently using Intellij Idea Community Edition as our Java development IDE.
- Click on the master branch to clone the repository to your local machine. Most IDEs can install Git extensions, so you can use the IDE to clone this repository. It's recommended to use[sourceTree](https://www.sourcetreeapp.com/)
- Once you have cloned the repository to your local machine, open this project with Intellij Idea Community. Intellij Idea Community will prompt you to install the extensions for this project. Click on install.

###### 2. Install Mysql Server And Mysql Workbench
2.1 Download [MySqlServer](https://dev.mysql.com/downloads/installer/) And MySQL Workbench [MySqlWorkbench](https://dev.mysql.com/downloads/workbench/), Install MySQL Server and MySQL Workbench,    
In MySQL Server, set the account for localhost to root, and set the password to Root1234.

2.2 Create localhost server:
- Connection Name : Please decide on a memorable name.
- Hostname : localhost
- Port : 3306
- Username : root
- Password : Root1234

2.3 In MySQL Workbench, create a Schema called "toys".   
2.4 Copy [init.sql](./src/init/init.sql) the contents inside, then execute in that schema.  
   
