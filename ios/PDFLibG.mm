#import "PDFLibG.h"

#import <PDFKit/PDFKit.h>

@implementation PDFLibG

RCT_EXPORT_MODULE()

RCT_REMAP_METHOD(encryptPDF,
                 :(NSString*)pdfPath
                 :(NSString*)pdfPassword
                 encryptPDFResolve:(RCTPromiseResolveBlock)resolve
                 encryptPDFReject:(RCTPromiseRejectBlock)reject)
{
    NSURL *url = [NSURL fileURLWithPath:pdfPath];
    if (@available(iOS 11.0, *)) {
        PDFDocument *document = [[PDFDocument alloc]initWithURL:url];
        
        NSDictionary *pdfOptions = [NSDictionary dictionaryWithObjectsAndKeys: pdfPassword, kCGPDFContextOwnerPassword, pdfPassword, kCGPDFContextUserPassword, nil];
        
        BOOL result = [document writeToFile:pdfPath withOptions:pdfOptions];
        NSMutableDictionary *response =  [[NSMutableDictionary alloc] init];
        response[@"result"] = @(result).stringValue;
        resolve(response);
    } else {
        resolve(@"Failed");
        // Fallback on earlier versions
    }
}

RCT_REMAP_METHOD(isEncrypted,
                 :(NSString*)pdfPath
                 isEncryptedResolve:(RCTPromiseResolveBlock)resolve
                 isEncryptedReject:(RCTPromiseRejectBlock)reject)
{
    NSURL *url = [NSURL fileURLWithPath:pdfPath];
    if (@available(iOS 11.0, *)) {
        PDFDocument *document = [[PDFDocument alloc]initWithURL:url];
        
        BOOL isLocked = [document isEncrypted];
        NSMutableDictionary *response =  [[NSMutableDictionary alloc] init];
        response[@"encrypted"] = @(isLocked).stringValue;
        resolve(response);
    } else {
        resolve(@"Failed");
        // Fallback on earlier versions
    }
}

RCT_REMAP_METHOD(decryptPDF,
                 :(NSString*)pdfPath
                 :(NSString*)pdfPassword
                 decryptPDFResolve:(RCTPromiseResolveBlock)resolve
                 decryptPDFReject:(RCTPromiseRejectBlock)reject)
{
    NSMutableDictionary *response =  [[NSMutableDictionary alloc] init];
    
    NSURL *url = [NSURL fileURLWithPath:pdfPath];
    if (@available(iOS 11.0, *)) {
        PDFDocument *document = [[PDFDocument alloc]initWithURL:url];
        
        NSDictionary *pdfOptions = [NSDictionary dictionaryWithObjectsAndKeys: @"", kCGPDFContextOwnerPassword, @"", kCGPDFContextUserPassword, nil];
        [document unlockWithPassword:pdfPassword];
        BOOL result = [document writeToFile:pdfPath withOptions:pdfOptions];
        response[@"result"] = @(result).stringValue;
    } else {
        response[@"result"] = @"0";
    }
    resolve(response);
}

@end































