#sensitivity of m: number of zones
data=read.table("Experiment/Chanhassen/EffectOfM.txt",sep=",")
x = 2:10;
y = data[,8]
ys = matrix(y, nr = 4)
par(mar=c(4,6,4,6))
plot(x,ys[1,],pch=0,col=1,cex.lab=2,cex=2,cex.axis=2, cex.main=2, xlab="Number of zones (m)",ylab="F-score");#,main=paste("Sensitivity of F-measure to alpha"));
lines(x,ys[1,],lty=4,lwd=2,col=1)
points(x,ys[2,],pch=3,cex=2,col=2); lines(x,ys[2,],lty=2,lwd=2,col=2)
points(x,ys[3,],pch=2,cex=2,col=3); lines(x,ys[3,],lty=3,lwd=2,col=3)
points(x,ys[4,],pch=1,cex=2,col=4); lines(x,ys[4,],lty=5,lwd=2,col=4)

legend(locator(1),c("SE with Single Model","SE with Bagging","SE with Boosting","SE with Random Forest"),lty=c(4,3,2,1),pch=c(0,3,2,1),cex=c(1.5,1.5,1.5,1.5),col=c(1,2,3,4))
dev.copy(postscript,"Experiment/Figures/mChanhassen.eps")
dev.off()

data=read.table("Experiment/BigStone/EffectOfM.txt",sep=",")
x = seq(2,40,by=2);
y = data[,8]
ys = matrix(y, nr = 4)
par(mar=c(4,6,4,6))
plot(x,ys[1,],pch=0,col=1,cex.lab=2,cex=2,cex.axis=2, cex.main=2, ylim=range(ys), xlab="Number of zones (m)",ylab="F-score");#,main=paste("Sensitivity of F-measure to alpha"));
lines(x,ys[1,],lty=4,lwd=2,col=1)
points(x,ys[2,],pch=3,cex=2,col=2); lines(x,ys[2,],lty=2,lwd=2,col=2)
points(x,ys[3,],pch=2,cex=2,col=3); lines(x,ys[3,],lty=3,lwd=2,col=3)
points(x,ys[4,],pch=1,cex=2,col=4); lines(x,ys[4,],lty=5,lwd=2,col=4)

legend(locator(1),c("SE with Single Model","SE with Bagging","SE with Boosting","SE with Random Forest"),lty=c(4,3,2,1),pch=c(0,3,2,1),cex=c(1.5,1.5,1.5,1.5),col=c(1,2,3,4))
dev.copy(postscript,"Experiment/Figures/mBigStone.eps")
dev.off()


#Effect of K
data=read.table("Experiment/Chanhassen/EffectOfK.txt",sep=",")
x = seq(5, 25, by = 5);
y = data[,8]
ys = matrix(y, nr = 4)
par(mar=c(4,6,4,6))
plot(x,ys[1,],pch=0,col=1,cex.lab=2,cex=2,cex.axis=2, cex.main=2, ylim=range(0.85,0.95), xlab="Class ambiguity measure parameter (k)",ylab="F-score");#,main=paste("Sensitivity of F-measure to alpha"));
lines(x,ys[1,],lty=4,lwd=2,col=1)
points(x,ys[2,],pch=3,cex=2,col=2); lines(x,ys[2,],lty=2,lwd=2,col=2)
points(x,ys[3,],pch=2,cex=2,col=3); lines(x,ys[3,],lty=3,lwd=2,col=3)
points(x,ys[4,],pch=1,cex=2,col=4); lines(x,ys[4,],lty=5,lwd=2,col=4)

legend(locator(1),c("SE with Single Model","SE with Bagging","SE with Boosting","SE with Random Forest"),lty=c(4,3,2,1),pch=c(0,3,2,1),cex=c(1.5,1.5,1.5,1.5),col=c(1,2,3,4))
dev.copy(postscript,"Experiment/Figures/kChanhassen.eps")
dev.off()

data=read.table("Experiment/BigStone/EffectOfK.txt",sep=",")
x = seq(5, 25, by = 5);
y = data[,8]
ys = matrix(y, nr = 4)
par(mar=c(4,6,4,6))
plot(x,ys[1,],pch=0,col=1,cex.lab=2,cex=2,cex.axis=2, cex.main=2, ylim=range(0.8,0.95), xlab="Class ambiguity measure parameter (k)",ylab="F-score");#,main=paste("Sensitivity of F-measure to alpha"));
lines(x,ys[1,],lty=4,lwd=2,col=1)
points(x,ys[2,],pch=3,cex=2,col=2); lines(x,ys[2,],lty=2,lwd=2,col=2)
points(x,ys[3,],pch=2,cex=2,col=3); lines(x,ys[3,],lty=3,lwd=2,col=3)
points(x,ys[4,],pch=1,cex=2,col=4); lines(x,ys[4,],lty=5,lwd=2,col=4)

legend(locator(1),c("SE with Single Model","SE with Bagging","SE with Boosting","SE with Random Forest"),lty=c(4,3,2,1),pch=c(0,3,2,1),cex=c(1.5,1.5,1.5,1.5),col=c(1,2,3,4))
dev.copy(postscript,"Experiment/Figures/kBigStone.eps")
dev.off()



#Effect of Base Classifier Type
sedat=read.table("Experiment/Chanhassen/EffectOfBase.txt",sep=",")
gdat=read.table("Experiment/Global/GlobalBaseChanhassen.txt",sep=",")
dat = rbind(gdat[,7],sedat[,7])
type=c("DT","SVM","NN","LR")
colnames(dat)=type
dev.new()
par(xpd=TRUE)
barplot(dat, ylab="F-score", width=0.08, xlim=c(0,1),ylim=c(0,1),beside=T,col=rainbow(2), cex=2,cex.lab=2,cex.axis=2)
legend(locator(1),c("Global Single Model","SE with Single Model"), cex=2, bty="n", fill=rainbow(2))	
dev.copy(postscript,"Experiment/Figures/baseChanhassen.eps")
dev.off()

sedat=read.table("Experiment/BigStone/EffectOfBase.txt",sep=",")
gdat=read.table("Experiment/Global/GlobalBaseBigStone.txt",sep=",")
dat = rbind(gdat[,7],sedat[,7])
type=c("DT","SVM","NN","LR")
colnames(dat)=type
dev.new()
par(xpd=TRUE)
barplot(dat, ylab="F-score", width=0.08, xlim=c(0,1),ylim=c(0,1),beside=T,col=rainbow(2), cex=2,cex.lab=2,cex.axis=2)
legend(locator(1),c("Global Single Model","SE with Single Model"), cex=2, bty="n", fill=rainbow(2))	
dev.copy(postscript,"Experiment/Figures/baseBigStone.eps")
dev.off()



#Effect of Alpha
data=read.table("Experiment/Chanhassen/EffectOfAlpha.txt",sep=",")
x = seq(0, 1, by = 0.1);
y = data[,8]
ys = matrix(y, nr = 4)
par(mar=c(4,6,4,6))
plot(x,ys[1,],pch=0,col=1,cex.lab=2,cex=2,cex.axis=2, cex.main=2, ylim=range(0.8,0.95), xlab="Spatial Ensemble Heuristic Parameter (alpha)",ylab="F-score");#,main=paste("Sensitivity of F-measure to alpha"));
lines(x,ys[1,],lty=4,lwd=2,col=1)
points(x,ys[2,],pch=3,cex=2,col=2); lines(x,ys[2,],lty=2,lwd=2,col=2)
points(x,ys[3,],pch=2,cex=2,col=3); lines(x,ys[3,],lty=3,lwd=2,col=3)
points(x,ys[4,],pch=1,cex=2,col=4); lines(x,ys[4,],lty=5,lwd=2,col=4)

legend(locator(1),c("SE with Single Model","SE with Bagging","SE with Boosting","SE with Random Forest"),lty=c(4,3,2,1),pch=c(0,3,2,1),cex=c(1.5,1.5,1.5,1.5),col=c(1,2,3,4))
dev.copy(postscript,"Experiment/Figures/alphaChanhassen.eps")
dev.off()



data=read.table("Experiment/BigStone/EffectOfAlpha.txt",sep=",")
x = seq(0, 1, by = 0.1);
y = data[,8]
ys = matrix(y, nr = 4)
par(mar=c(4,6,4,6))
plot(x,ys[1,],pch=0,col=1,cex.lab=2,cex=2,cex.axis=2, cex.main=2, ylim=range(0.7,0.9), xlab="Spatial Ensemble Heuristic Parameter (alpha)",ylab="F-score");#,main=paste("Sensitivity of F-measure to alpha"));
lines(x,ys[1,],lty=4,lwd=2,col=1)
points(x,ys[2,],pch=3,cex=2,col=2); lines(x,ys[2,],lty=2,lwd=2,col=2)
points(x,ys[3,],pch=2,cex=2,col=3); lines(x,ys[3,],lty=3,lwd=2,col=3)
points(x,ys[4,],pch=1,cex=2,col=4); lines(x,ys[4,],lty=5,lwd=2,col=4)

legend(locator(1),c("SE with Single Model","SE with Bagging","SE with Boosting","SE with Random Forest"),lty=c(4,3,2,1),pch=c(0,3,2,1),cex=c(1.5,1.5,1.5,1.5),col=c(1,2,3,4))
dev.copy(postscript,"Experiment/Figures/alphaBigStone.eps")
dev.off()

#Effect of Np
data=read.table("Experiment/BigStone/EffectOfNp.txt",sep=",")
x = seq(600, 2400, by = 200);
y = data[,8]
ys = matrix(y, nr = 4)
par(mar=c(4,6,4,6))
plot(x,ys[1,],pch=0,col=1,cex.lab=2,cex=2,cex.axis=2, cex.main=2, ylim=range(ys), xlab="Spatial Ensemble Heuristic Parameter (alpha)",ylab="F-score");#,main=paste("Sensitivity of F-measure to alpha"));
lines(x,ys[1,],lty=4,lwd=2,col=1)
points(x,ys[2,],pch=3,cex=2,col=2); lines(x,ys[2,],lty=2,lwd=2,col=2)
points(x,ys[3,],pch=2,cex=2,col=3); lines(x,ys[3,],lty=3,lwd=2,col=3)
points(x,ys[4,],pch=1,cex=2,col=4); lines(x,ys[4,],lty=5,lwd=2,col=4)

legend(locator(1),c("SE with Single Model","SE with Bagging","SE with Boosting","SE with Random Forest"),lty=c(4,3,2,1),pch=c(0,3,2,1),cex=c(1.5,1.5,1.5,1.5),col=c(1,2,3,4))
dev.copy(postscript,"Experiment/Figures/NpBigStone.eps")
dev.off()


#computational time cost on np
faster = read.table("Experiment/Chanhassen/HMergeFaster.time.txt", sep=",")
base = read.table("Experiment/Chanhassen/HMergeBaseline.time.txt", sep=",")

dev.new();
par(mar=c(4,6,4,6))
plot(faster, ylim = range(faster[,2],base[,2]), xlim=rev(range(faster[,1])), type="n", cex.lab=2,cex=2,cex.axis=2, cex.main=2, xlab="The number of patches n",ylab="Time cost (second)")
lines(faster, lty = 1)
points(base, type="n")
lines(base, lty=2)
legend(locator(1), c("Refined","Baseline"), lty=c(1,2), cex=c(2,2))
dev.copy(postscript, "Experiment/Figures/time.eps")
dev.off();