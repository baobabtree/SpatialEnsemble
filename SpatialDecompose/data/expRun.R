require(raster)
require(rpart)
set.seed(ceiling(sqrt(4301901)))
ref = raster("Chanhassen/ref.img")
features = stack("Chanhassen/spectral.img")

nc = ref@ncols
loc = 1:ncell(ref)
loc.r = (loc-1)%/%nc+1
loc.c = (loc-1)%%nc + 1

input = data.frame(values(features[[1]]), values(features[[2]]), values(features[[3]]), values(features[[4]]), values(ref), loc.r, loc.c)


#sample 1: Clustered Sampling
# N = 30
# trainWetRep=sample(loc.raster[ref==1],N)
# trainDryRep=sample(loc.raster[ref==0],N)
# trainRep = c(trainWetRep, trainDryRep)

#sample 2: systematic clustered sampling
num.per.cell = 1;
all.cell.number=1:(ref@ncols*ref@nrows)
grid.size=50
N = 2
grid.nrows=ceiling(ref@nrows/grid.size)
grid.ncols=ceiling(ref@ncols/grid.size)
i.vec=rep(1:ref@nrows,each=ref@ncols)
j.vec=rep(1:ref@ncols,times=ref@nrows)
i.grid.vec=ceiling(i.vec/grid.size)
j.grid.vec=ceiling(j.vec/grid.size)
grid.number=(i.grid.vec-1)*grid.ncols+j.grid.vec
trainRep = NULL;
for(g in 1:(grid.nrows*grid.ncols)){
	#randomly select several samples
	trainRep.g = sample(all.cell.number[grid.number==g], N)
	trainRep = c(trainRep, trainRep.g);
	#grid.majority.class[g]=modal(ref[which(grid.number==g)])
}

#clustered sample
train=ref;
train[1:ncell(train)]=NA
loc.raster = ref
loc.raster[1:ncell(ref)]=loc
train[c(trainRep)]=1
radius=21
train=buffer(train,width=radius) #note here the width is meters, b/3=no.of.cell
dev.new(); plot(train, col=c("red","green"))
#plot training set locations
train.map = ref; train.map[data[,9]>0] = NA; 
dev.new(); plot(train.map, col=c("red","green"))


input[,5] = input[,5] +1;
input[is.na(values(train)), 5] = 0
write.table(input, "Chanhassen/input.txt",sep=",", row.n=F, col.n=F)

#read filtered points
data=read.table("Chanhassen/input.texture.txt",sep=",")
data[,9] = values(ref) + 1;
data[is.na(values(train)),9] = 0;
#write.table(data,"Chanhassen/input.texture.txt",sep=",", row.n=F, col.n=F)

#read clusters: (pid, cid, label)
cls = read.table("Chanhassen/cluster.txt",sep=",")
cluster.ids=unique(cls[,2])
dict=array(0,max(cluster.ids)+1)
dict[cluster.ids+1]=sample(length(cluster.ids))
c.map=ref;
c.map[cls[,1]]=dict[cls[,2]+1]

jet.colors <-
  colorRampPalette(c("#00007F", "blue", "#007FFF", "cyan",
                     "#7FFF7F", "yellow", "#FF7F00", "red", "#7F0000"))
dev.new();plot(c.map,col=jet.colors(length(cluster.ids)))


#read footprints from BisectSpatialEnsemble fid-pid
footprints = read.table("Chanhassen/footprints.3.txt",sep=",")
footprint.map = ref;
footprint.map[1:ncell(ref)]=NA
for(fid in unique(footprints[,1])){
	footprint.map[footprints[footprints[,1]==fid,2]+1] = fid;
}
dev.new();plot(footprint.map, col=jet.colors(length(unique(footprints[,1]))))

#get per footprint prediction accuracy
#footprint1
res = matrix(0, nr=2,nc=2);
for(i in unique(footprints[,1])){
	data.i = data[footprints[footprints[,1]==i,2]+1,];
	train.i = data.i[data.i[,9]>0,1:9]
	ref.i = ref[footprints[footprints[,1]==i,2]+1]
	if(length(unique(train.i[,9]))==1){
		dt.pred.i = rep(unique(train.i[,9]), length=nrow(data.i)) ;
		res.i = matrix(0,nr=2,nc=2);
		res.i[,unique(train.i[,9])] = as.numeric(table(ref.i));
	}
	else{
		dt.i = rpart(V9~., data=train.i, method="class");
		dt.pred.i = predict(dt.i, newdata = data.i, type="class");	
		res.i = table(ref.i, dt.pred.i);
	}

	print(res.i)
	res = res + as.matrix(res.i);
}
print(res)




#test decision tree
require(rpart)
dt = rpart(V9~., data=data[data[,9]>0,1:9],method="class");
dt.pred = predict(dt, newdata = data[,1:9],type="class")
table(values(ref),dt.pred)
dt.map = ref; 
dt.map[1:ncell(ref)] = as.numeric(dt.pred)-1;
dt.map[dt.map==0&ref==1] = 2;
dt.map[dt.map==1&ref==0] = 3;
dev.new();plot(dt.map, col=c("red","green","black","blue"))