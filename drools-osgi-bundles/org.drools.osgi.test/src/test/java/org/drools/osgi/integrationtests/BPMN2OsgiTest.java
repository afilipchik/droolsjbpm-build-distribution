/*
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.osgi.integrationtests;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactoryService;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderError;
import org.drools.builder.KnowledgeBuilderFactoryService;
import org.drools.builder.ResourceType;
import org.drools.io.ResourceFactoryService;
import org.drools.osgi.test.AbstractDroolsSpringDMTest;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.process.ProcessInstance;
import org.drools.util.ServiceRegistry;
import org.osgi.framework.ServiceReference;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class BPMN2OsgiTest extends AbstractDroolsSpringDMTest {

    protected void onSetUp() throws Exception {
        ServiceReference kbuilderRef = bundleContext.getServiceReference( KnowledgeBuilderFactoryService.class.getName() );
        Thread.currentThread().setContextClassLoader(  bundleContext.getService( kbuilderRef ).getClass().getClassLoader()  );
    }

    protected void onTearDown() throws Exception {
        
    }

    @Test
    public void testMinimalProcess() throws Exception {
        KnowledgeBase kbase = createKnowledgeBase("BPMN2-MinimalProcess.xml");
        StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
        ProcessInstance processInstance = ksession.startProcess("Minimal");
        assertTrue(processInstance.getState() == ProcessInstance.STATE_COMPLETED);
    }
    
    private KnowledgeBase createKnowledgeBase(String process) throws Exception {
        
        ServiceReference serviceRef = bundleContext.getServiceReference( ServiceRegistry.class.getName() );
        ServiceRegistry registry = (ServiceRegistry) bundleContext.getService( serviceRef );

        KnowledgeBuilderFactoryService knowledgeBuilderFactoryService = registry.get( KnowledgeBuilderFactoryService.class );
        KnowledgeBaseFactoryService knowledgeBaseFactoryService = registry.get( KnowledgeBaseFactoryService.class );
        ResourceFactoryService resourceFactoryService = registry.get( ResourceFactoryService.class );
        
        KnowledgeBuilder kbuilder = knowledgeBuilderFactoryService.newKnowledgeBuilder();
        kbuilder.add(resourceFactoryService.newClassPathResource(process, BPMN2OsgiTest.class), ResourceType.BPMN2);
        
//        KnowledgeBuilderConfiguration conf = knowledgeBuilderFactoryService.newKnowledgeBuilderConfiguration();
//        ((PackageBuilderConfiguration) conf).initSemanticModules();
//        ((PackageBuilderConfiguration) conf).addSemanticModule(new BPMNSemanticModule());
//        ((PackageBuilderConfiguration) conf).addSemanticModule(new BPMN2SemanticModule());
//        ((PackageBuilderConfiguration) conf).addSemanticModule(new BPMNDISemanticModule());
//        ((PackageBuilderConfiguration) conf).addDialect("XPath", new XPathDialectConfiguration());
//        
//        XmlProcessReader processReader = new XmlProcessReader(
//            ((PackageBuilderConfiguration) conf).getSemanticModules());
//        RuleFlowProcess p = (RuleFlowProcess)
//            processReader.read(BPMN2OsgiTest.class.getResourceAsStream(process));
//        
//        KnowledgeBuilder kbuilder = knowledgeBuilderFactoryService.newKnowledgeBuilder(conf);
//        
//        kbuilder.add(resourceFactoryService.newReaderResource(
//            new StringReader(XmlBPMNProcessDumper.INSTANCE.dump(p))), ResourceType.DRF);
        
        if (!kbuilder.getErrors().isEmpty()) {
            for (KnowledgeBuilderError error: kbuilder.getErrors()) {
                System.err.println(error);
            }
            throw new IllegalArgumentException("Errors while parsing knowledge base:\n" + kbuilder.getErrors());
        }
        KnowledgeBase kbase = knowledgeBaseFactoryService.newKnowledgeBase();
        kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
        return kbase;
    }

}
