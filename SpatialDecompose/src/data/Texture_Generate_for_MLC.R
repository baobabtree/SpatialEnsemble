require(raster)
require(rgdal)
require(rpart)
require(randomForest)
require(maptools)

setwd(dirname(rstudioapi::getActiveDocumentContext()$path))
set.seed(ceiling(sqrt(4301901)))
options(scipen = 999)

features = stack("BigStone/features.tif")
# ext = extent(features)
# ext = extent(-77.327, -77.271, 35.586, 35.619) # presentation
# features = crop(features, ext)


# for (i in 1:3){
#   value = values(features[[1]])
#   value = as.raw(values(features[[1]]))
#   value <- sapply(value, rawToChar)
#   writeBin(value, con = paste(getwd(),"/Texture.",1,".bin",sep=""), size = 1)
#   dput(value, file = paste(getwd(),"/Test.asc",sep=""))
# }


# # grey tone is from 0 to 255
# interval=values(features[[1]])
# for (i in 1:length(interval)){
#   interval[i]=interval[i] %/% 8
# }
# features[[1]]=interval
# 
# interval=values(features[[2]])
# for (i in 1:length(interval)){
#   interval[[i]]=interval[[i]] %/% 8
# }
# features[[2]]=interval
# 
# interval=values(features[[3]])
# for (i in 1:length(interval)){
#   interval[[i]]=interval[[i]] %/% 8
# }
# features[[3]]=interval
# # now values is from 1 to 26 %/%10, 1 to 32 %/% 8

# for (i in 1:3){
#   dryValue = values(features[[i]])
#   dryValue = dryValue[dryCell.index]
#   # dryValue = all.data[dryCell.index,i]
#   features[[i]]=NA
#   features[[i]][dryCell.index] = dryValue
#   
#   floodValue = values(features[[i]])
#   floodValue = floodValue[floodCell.index]
#   # floodValue = all.data[floodCell.index,i]
#   features[[i]]=NA
#   features[[i]][floodCell.index] = floodValue
# }


for (i in 1:4){
  interval=values(features[[i]])
  interval=interval %/% 8
  features[[i]]=interval
}

textures = features;

# because R stores matrix by column, so the co-occurence matrix here is vertical
window.edge = 5
window.size = window.edge^2
normal_factor = 2*window.edge*(window.edge-1) # total number of co-occurrence pairs
for (i in 1:4){
  time_start=Sys.time()
  
  focaled = focal(features[[i]], w=matrix(1, window.edge, window.edge),fun=function(x,na.rm=TRUE){
    co_matrix = matrix(0,32,32)
    for (i in 1:window.size){
      if (i %% window.edge != 0){
        if(!is.na(x[i]) && !is.na(x[i+1])){
          co_matrix[x[i],x[i+1]] <- co_matrix[x[i],x[i+1]]+1
          co_matrix[x[i+1],x[i]] <- co_matrix[x[i+1],x[i]]+1
        }
      }
    }
    HOM = 0
    for (i in 1:32)
      for(j in 1:32){
        HOM <- HOM + co_matrix[i,j]/(1+abs(i-j))
      }
    return(HOM)
  },
  na.rm=TRUE, pad=TRUE)
  
  time_end = Sys.time()
  print(time_end-time_start)

 textures[[i]] = focaled;
 #writeRaster(focaled1,filename=paste(getwd(),"focal1.studyArea.tif",sep="/"),format="GTiff",overwrite=TRUE)
#  writeRaster(focaled,filename=paste("focal",i,".StudyArea.5x5.tif",sep=""),format="GTiff",overwrite=TRUE)
}


# time_start=Sys.time()
# window.edge = 5
# window.size = window.edge^2
# normal_factor = 2*window.edge*(window.edge-1) # total number of co-occurrence pairs
# focaled1 = focal(features[[1]], w=matrix(1, window.edge, window.edge),fun=function(x,na.rm=TRUE){
#   co_matrix = matrix(0,32,32)
#   for (i in 1:window.size){
#     if (i %% window.edge != 0){
#       if(!is.na(x[i]) && !is.na(x[i+1])){
#         co_matrix[x[i],x[i+1]] <- co_matrix[x[i],x[i+1]]+1
#         co_matrix[x[i+1],x[i]] <- co_matrix[x[i+1],x[i]]+1
#       }
#     }
#   }
#   HOM = 0
#   for (i in 1:32)
#     for(j in 1:32){
#       HOM <- HOM + co_matrix[i,j]/(1+abs(i-j))
#     }
#   return(HOM)
# },
# na.rm=TRUE, pad=TRUE)
# time_end = Sys.time()
# print(time_end-time_start)
# #writeRaster(focaled1,filename=paste(getwd(),"focal1.studyArea.tif",sep="/"),format="GTiff",overwrite=TRUE)
# writeRaster(focaled1,filename="focal1.StudyArea.5x5.tif",format="GTiff",overwrite=TRUE)
# 
# 
# time_start=Sys.time()
# focaled2 = focal(features[[2]],w=matrix(1, window.edge, window.edge),fun=function(x,na.rm=TRUE){
#   co_matrix = matrix(0,32,32)
#   for (i in 1:window.size){
#     if (i %% window.edge != 0){
#       if(!is.na(x[i]) && !is.na(x[i+1])){
#         co_matrix[x[i],x[i+1]] <- co_matrix[x[i],x[i+1]]+1
#         co_matrix[x[i+1],x[i]] <- co_matrix[x[i+1],x[i]]+1
#       }
#     }
#   }
#   HOM = 0
#   for (i in 1:32)
#     for(j in 1:32){
#       HOM <- HOM + co_matrix[i,j]/(1+abs(i-j))
#     }
#   return(HOM)
# },
# na.rm=TRUE, pad=TRUE)
# time_end = Sys.time()
# print(time_end-time_start)
# #writeRaster(focaled2,filename=paste(getwd(),"focal2.studyArea.tif",sep="/"),format="GTiff",overwrite=TRUE)
# writeRaster(focaled2,filename=paste(getwd(),"focal2.StudyArea.5x5.tif",sep="/"),format="GTiff",overwrite=TRUE)
# 
# 
# 
# time_start=Sys.time()
# focaled3 = focal(features[[3]],w=matrix(1, window.edge, window.edge),fun=function(x,na.rm=TRUE){
#   co_matrix = matrix(0,32,32)
#   for (i in 1:window.size){
#     if (i %% window.edge != 0){
#       if(!is.na(x[i]) && !is.na(x[i+1])){
#         co_matrix[x[i],x[i+1]] <- co_matrix[x[i],x[i+1]]+1
#         co_matrix[x[i+1],x[i]] <- co_matrix[x[i+1],x[i]]+1
#       }
#     }
#   }
#   HOM = 0
#   for (i in 1:32)
#     for(j in 1:32){
#       HOM <- HOM + co_matrix[i,j]/(1+abs(i-j))
#     }
#   return(HOM)
# },
# na.rm=TRUE, pad=TRUE)
# time_end = Sys.time()
# print(time_end-time_start)
# #writeRaster(focaled3,filename=paste(getwd(),"focal3.studyArea.tif",sep="/"),format="GTiff",overwrite=TRUE)
# writeRaster(focaled3,filename=paste(getwd(),"focal3.StudyArea.5x5.tif",sep="/"),format="GTiff",overwrite=TRUE)


# end matrix --------------------------------------------------------------

data_frame=data.frame(band1=values(features[[1]]), band2=values(features[[2]]), band3=values(features[[3]]),
                      texture1=values(focaled1),texture2=values(focaled2),texture3=values(focaled3),class=0)
class_column=7
#saveRDS(data_frame,file="D:/Miao/MyR/Flood_Prediction/Sampled/data_frame.original.rds")
# data_frame=readRDS("D:/Miao/MyR/Flood_Prediction/Sampled/data_frame.original.rds")

floodCell_index=unlist(floodCell_index)
dryCell_index=unlist(dryCell_index)


# data_frame[floodCell_index,4]=1
# data_frame[,4] = as.factor(data_frame[,4]) # too slow

data_frame[floodCell_index,class_column]=1 # when read data_frame.original.rds and run with, need to remember set flood class to 1
train.index=c(floodCell_index,sample(dryCell_index,length(floodCell_index)))
train.data = data_frame[train.index,]
train.data[,class_column]=as.factor(train.data[,class_column])

fit.all = rpart(class~band1+band2+band3+texture1+texture2+texture3, data = train.data, method = "class",minbucket=floor(dim(train.data)[[1]]/100))
# saveRDS(fit.all,file="D:/Miao/MyR/Flood_Prediction/Small_Polygon/fit.all.rds")
# fit.all=readRDS("D:/Miao/MyR/Flood_Prediction/Small_Polygon/fit.all.rds")
##Error in cbind(yval2, yprob, nodeprob) : number of rows of matrices must match (see arg 2). If all of your left-hand side values are the same

predict.data=data_frame[predict.index,]
predict.data[,class_column]=as.factor(predict.data[,class_column])

preds.map = predict(fit.all, predict.data, type = "class")


## flood_raster[rep(TRUE,ncell(flood_raster))]= -1 #31.2min
## flood_raster[1:ncell(flood_raster)]=-1 #25.9min
## flood_raster[1:ncell(flood_raster)]=rep(-1,ncell(flood_raster)) #26.5min

## can't use saveRDS here, not gonna work
## saveRDS(flood_raster,file="D:/Miao/MyR/Flood_Prediction/Use_Predicted_Label_to_Retrain/flood_raster_all-1.rds")
## flood_raster=readRDS("D:/Miao/MyR/Flood_Prediction/Use_Predicted_Label_to_Retrain/flood_raster_all-1.rds")

## when reading raster file, the crs will become the default value, adding crs parameter does not work either
## writeRaster(flood_raster,filename="D:/Miao/MyR/Flood_Prediction/Small_Polygon/test.tif",format="GTiff",overwrite=TRUE,crs=shape.flood@proj4string)
## test=raster("D:/Miao/MyR/Flood_Prediction/Small_Polygon/test.tif",crs=shape.flood@proj4string)

flood_raster=features[[1]]

# flood_raster[rep(TRUE,ncell(flood_raster))]= rep(-1,ncell(flood_raster)) #25.7min, faster
# Or, assign -1 to all empty cells, save time
flood_raster[!predict.index]=-1

# writeRaster(flood_raster,filename="D:/Miao/MyR/Flood_Prediction/Small_Polygon/flood_raster_all-1.tif",format="GTiff",overwrite=TRUE)
# flood_raster=raster("D:/Miao/MyR/Flood_Prediction/Use_Predicted_Label_to_Retrain/flood_raster_all-1.tif")
# crs(flood_raster)=shape.flood@proj4string

flood_raster[predict.index]=as.numeric(preds.map)-1
writeRaster(flood_raster,filename="D:/Miao/MyR/Flood_Prediction/Texture_added/flood_raster_texture_added.tif",format="GTiff",overwrite=TRUE)

time_1=Sys.time()
time=time_1-time_0
print(time)
