#import <Foundation/Foundation.h>
#import "PDFWriterFactory.h"
#import "PDFPageFactory.h"

PDFWriterFactory::PDFWriterFactory (PDFWriter* pdfWriter) {
    this->pdfWriter = pdfWriter;
}

NSString* PDFWriterFactory::create (NSDictionary* documentActions) {
    NSString *path = documentActions[@"path"];
    NSLog(@"%@%@", @"Creating document at: ", path);
    PDFWriter pdfWriter;
    EStatusCode esc;
    PDFWriterFactory factory(&pdfWriter);
    
    esc = pdfWriter.StartPDF(path.UTF8String, ePDFVersion13);
    if (esc == EStatusCode::eFailure) {
        return nil;
    }
    
    // Process pages
    factory.addPages(documentActions[@"pages"]);
    
    esc = pdfWriter.EndPDF();
    if (esc == EStatusCode::eFailure) {
        return nil;
    }
    
    return path;
}

NSString* PDFWriterFactory::modify(NSDictionary* documentActions) {
    NSString *path = documentActions[@"path"];
    NSLog(@"%@%@", @"Creating document at: ", path);
    PDFWriter pdfWriter;
    EStatusCode esc;
    PDFWriterFactory factory(&pdfWriter);
    
    NSString *password = documentActions[@"password"];
    NSLog(@"%@%@", @"Document Password: ", password);
    
    if ([password length] > 0) {
        NSLog(@"In IF COnd ");
        NSString *passPath = documentActions[@"passPath"];
        NSLog(@"%@%@", @"Document passPath: ", passPath);
        esc = pdfWriter.ModifyPDF(path.UTF8String, ePDFVersion13, passPath.UTF8String, LogConfiguration::DefaultLogConfiguration(), PDFCreationSettings(true,true, EncryptionOptions(password.UTF8String,4,password.UTF8String)));
        
        NSLog(@"%d", @"Document Modified Set Password: ", esc);
        if (esc == EStatusCode::eFailure) {
            return nil;
        }
        esc = pdfWriter.EndPDF();
        NSLog(@"%d", @"Document Modified end Password: ", esc);
        if (esc == EStatusCode::eFailure) {
            return nil;
        }
        
        return passPath;
    } else {
        NSLog(@"In else COnd ");
        // Empty string to modify in place
        esc = pdfWriter.ModifyPDF(path.UTF8String, ePDFVersion13, @"".UTF8String);
        if (esc == EStatusCode::eFailure) {
            return nil;
        }
        
        // Add pages
        factory.addPages(documentActions[@"pages"]);
        
        // Modify pages
        factory.modifyPages(documentActions[@"modifyPages"]);
        
        esc = pdfWriter.EndPDF();
        if (esc == EStatusCode::eFailure) {
            return nil;
        }
    }
    
    return path;
}

void PDFWriterFactory::addPages (NSArray* pages) {
    for (NSDictionary *pageActions in pages) {
        PDFPageFactory::createAndWrite(pdfWriter, pageActions);
    }
}

void PDFWriterFactory::modifyPages (NSArray* pages) {
    for (NSDictionary *pageActions in pages) {
        PDFPageFactory::modifyAndWrite(pdfWriter, pageActions);
    }
}







































