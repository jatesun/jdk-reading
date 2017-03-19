# jdk-reading
jdk关键类的阅读注释，理解。针对java日常开发中使用最多的框架进行源码部分的解析，深入理解日常使用类的背后源码实现，并附上中文注释以便理解。由于只是分析所以源码没有用maven或者gradle构建，可能需要自己引入jdk（推荐1.8）
本工程将会不断更新，直到所有自己认为所有关键类分析完毕为止，下面是自己包结构。
## base
对应的模块为src/base/java。里面的模块是自己认为日常开发最常用的类，深入理解这些类会让我们日常开发用到这些类的时候更加的随心所欲，也能进一
步加深理解。主要包括java.lang里面的集合包、java.io和java.lang主要类。
#### 详细目录
├── com.jatesun.collection   //Java集合

	├── com.jatesun.collection.list  //Java集合List
	
	├── com.jatesun.collection.map   //Java集合Map
	
	├── com.jatesun.collection.other //Java集合其他杂项
	
	├── com.jatesun.collection.queue //Java集合queue
	
	├── com.jatesun.collection.set   //Java集合Set
## advance
对应模块为src/advance/java。里面的模块是自己认为日常开发中能够体现出初级跟中高级开发人员区别的包，这些包用的好坏、理解的深浅是
鉴定一个程序员是否合格的重要标志，同时也是向更深层次进阶的基础。主要包括反射、net包、nio相关、concurrent包相关。
# 最后
如果你对某个类或者模块分析的代码不熟悉，可以先熟悉使用，毕竟只有会用才能更好读懂源码、体会源码中的精华。这里推荐大家一个java核心学习的github项目（https://github.com/JeffLi1993/java-core-learning-example）
自己一直想系统的分析jdk源码来加深自己的基础，以前阅读jdk源码时间不算少，但是觉得效果有限，没有深刻的理解。jdk的代码每一行都是有意义的，每一行代码
都是最顶尖的java工程师费心的结果，值得我们去认真推敲，反复阅读。阅读jdk代码其实不是为了读，而是为了求道，仔细的看世界上最好的java工程师们写的代码
你自己的代码水平也会不知不觉得到提高，百利而无一害。另外，我的博客也会随着本工程一起，读到一个类就会对应一篇文章，这里的仅仅是源码注释，要看全局以及
深入分析理解请关注[我的博客](http://jatesun.github.io)。另外，如果喜欢欢迎star，如果你有兴趣一起分析，可以fork并提pull request。

