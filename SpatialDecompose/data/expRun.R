require(raster)
set.seed(ceiling(sqrt(4301901)))
ref = raster("../../../Experiment/Chanhassen/ref.img")
features = stack("../../../Experiment/Chanhassen/spectral.img")

nc = ref@ncols
loc = 1:ncell(ref)
loc.r = (loc-1)%/%nc+1
loc.c = (loc-1)%%nc + 1

input = data.frame(values(features[[1]]), values(features[[2]]), values(features[[3]]), values(features[[4]]), values(ref), loc.r, loc.c)

train=ref;
train[1:ncell(train)]=NA
N = 30
loc.raster = ref;
loc.raster[1:ncell(ref)]=loc
trainWetRep=sample(loc.raster[ref==1],N)
trainDryRep=sample(loc.raster[ref==0],N)
train[c(trainWetRep, trainDryRep)]=1
radius=21
train=buffer(train,width=radius) #note here the width is meters, b/3=no.of.cell

input[,5] = input[,5] +1;
input[is.na(values(train)), 5] = 0
write.table(input, "../../../Experiment/Chanhassen/chanhassen.input.txt",sep=",", row.n=F, col.n=F)

#read clusters: (pid, cid, label)
cls = read.table("../../../Experiment/Chanhassen/output.cluster.txt",sep=",")
cluster.ids=unique(cls[,2])
dict=array(0,max(cluster.ids)+1)
dict[cluster.ids+1]=sample(length(cluster.ids))
c.map=ref;
c.map[cls[,1]]=dict[cls[,2]+1]

jet.colors <-
  colorRampPalette(c("#00007F", "blue", "#007FFF", "cyan",
                     "#7FFF7F", "yellow", "#FF7F00", "red", "#7F0000"))
dev.new();plot(c.map,col=jet.colors(length(cluster.ids)))


#read footprints from last BipartiteEnsemble with fid-cid
footprints = read.table("../../../Experiment/Chanhassen/output.footprints.txt", sep=",");
footprint.map = ref;
footprint.map[1:ncell(ref)]=NA
for(fid in unique(footprints[,1])){
	for(cid in footprints[footprints[,1]==fid,2]){
		footprint.map[cls[cls[,2]==cid,1] + 1] = fid;
	}
}
dev.new();plot(footprint.map, col=c("red","green"))


#read footprints from BisectSpatialEnsemble fid-pid
footprints = read.table("../../../Experiment/Chanhassen/output.footprints.txt", sep=",");
footprint.map = ref;
footprint.map[1:ncell(ref)]=NA
for(fid in unique(footprints[,1])){
	footprint.map[footprints[footprints[,1]==fid,2]+1] = fid;
}
dev.new();plot(footprint.map, col=c("red","green"))