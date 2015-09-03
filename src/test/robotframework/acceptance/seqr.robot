*** Settings ***
Library         Process
Library         OperatingSystem
Library         Collections 
Suite Teardown          Terminate All Processes    

*** Variables ***
${in_fasta} =    testdata/data/test.fasta
${jar} =    target/seqr-4.10.4-SNAPSHOT.jar

*** Test Cases ***
TestProteinSearch
    ${process_result} =     Run Process     java    -jar    ${jar}    search	  ${in_fasta}	 --db	 testdata/solr/
    #TODO: refactor below into wrapper function
    # Check system exited  correctly
    Log To Console       ${process_result.stdout}
    Log To Console        ${process_result.stderr}
    Should Be Equal As Integers         ${process_result.rc}        0

    # Check output
    Should Contain         ${process_result.stdout}        3915347
    Should Contain         ${process_result.stdout}     spirochetes	Borrelia burgdorferi B31
    #Should Be Equal As Strings     ${actual_contents}      ${expected_contents}




#TestIndex
TestTranslationSearch
    ${process_result} =     Run Process     java    -jar   ${jar}   search  testdata/data/testnt.fasta      --db    testdata/solr/      --is_dna


    Log To Console       ${process_result.stdout}
    Log To Console        ${process_result.stderr}
    Should Be Equal As Integers         ${process_result.rc}        0

    # Check output
    Should Contain         ${process_result.stdout}        131090


TestSolrServerSearch
    ${make_result} =     Run Process     make    server
    Log To Console       ${make_result.stdout}
    Log To Console        ${make_result.stderr}
    Should Be Equal As Integers         ${make_result.rc}        0


    ${search_result} =     Run Process     java    -jar    ${jar}    search	  ${in_fasta}	 --db	 http://localhost:8983/solr/

    # Check system exited  correctly
    Log To Console       ${search_result.stdout}
    Log To Console        ${search_result.stderr}
    Should Be Equal As Integers         ${search_result.rc}        0

    # Check output
    Should Contain         ${search_result.stdout}        3915347
    Should Contain         ${search_result.stdout}     spirochetes	Borrelia burgdorferi B31
