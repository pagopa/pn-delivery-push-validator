package it.pagopa.pn.deliverypushvalidator.service;


import it.pagopa.pn.deliverypushvalidator.dto.ext.safestorage.FileCreationResponseInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.safestorage.FileCreationWithContentRequest;
import it.pagopa.pn.deliverypushvalidator.dto.ext.safestorage.FileDownloadResponseInt;
import it.pagopa.pn.deliverypushvalidator.dto.ext.safestorage.UpdateFileMetadataResponseInt;
import it.pagopa.pn.deliverypushvalidator.generated.openapi.msclient.pnsafestorage.model.UpdateFileMetadataRequest;
import reactor.core.publisher.Mono;

public interface SafeStorageService {
    Mono<FileDownloadResponseInt> getFile(String fileKey, Boolean metadataOnly) ;
    
    Mono<FileCreationResponseInt> createAndUploadContent(FileCreationWithContentRequest fileCreationRequest);

    Mono<UpdateFileMetadataResponseInt> updateFileMetadata(String fileKey, UpdateFileMetadataRequest updateFileMetadataRequest);

    Mono<byte[]> downloadPieceOfContent(String fileKey, String url, long maxSize);
}
