package com.zamolski.storagereplicator

import com.google.cloud.functions.CloudEventsFunction
import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.StorageOptions
import com.google.events.cloud.storage.v1.StorageObjectData
import com.google.protobuf.util.JsonFormat
import io.cloudevents.CloudEvent
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream
import org.slf4j.LoggerFactory
import java.nio.channels.Channels

const val DESTINATION_BUCKET_ENV = "BUCKET_NAME"

class ReplicatorFunction : CloudEventsFunction {

    private val logger = LoggerFactory.getLogger(this.javaClass)
    private val storage = StorageOptions.getDefaultInstance().service

    override fun accept(event: CloudEvent?) {
        val storageObjectDataBuilder = StorageObjectData.newBuilder()
        JsonFormat.parser().merge(event?.data?.toBytes()?.decodeToString(), storageObjectDataBuilder)
        val storageObjectData = storageObjectDataBuilder.build()
        logger.info("event type: ${event?.type}, bucket: ${storageObjectData.bucket}, file: ${storageObjectData.name}")
        compress(storageObjectData.bucket, storageObjectData.name)
    }

    private fun compress(sourceBucketName: String, sourceFileName: String) {
        val destinationBucketName = System.getenv(DESTINATION_BUCKET_ENV)
        val destinationFileName = "${sourceFileName}.gz"

        logger.info("Compressing $sourceFileName from $sourceBucketName to $destinationBucketName as $destinationFileName")

        val sourceBlobId = BlobId.of(sourceBucketName, sourceFileName)
        val destinationBlobId = BlobId.of(destinationBucketName, destinationFileName)

        logger.info("Source file size is ${storage[sourceBlobId].size} bytes.")

        Channels.newInputStream(storage.reader(sourceBlobId)).use { inputStream ->
            Channels.newOutputStream(storage.writer(BlobInfo.newBuilder(destinationBlobId).build())).use { outputStream ->
                GzipCompressorOutputStream(outputStream).use { gzipOutputStream ->
                    val buffer = ByteArray(1024)
                    var bytesRead: Int
                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        gzipOutputStream.write(buffer, 0, bytesRead)
                    }
                }
            }
        }

        logger.info("Compression completed. Resulting file size is ${storage[destinationBlobId].size} bytes.")
    }
}