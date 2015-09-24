
*** Settings ***
Library         Process
Library         OperatingSystem
Library         Collections
Suite Teardown          Terminate All Processes

*** Variables ***
${in_fasta} =    testdata/data/test.fasta
${jar} =    target/seqr-4.10.4-SNAPSHOT.jar
${newly_indexed_fasta} =    testdata/f1.fasta

*** Keywords ***
Should Be Equal As Files        [Arguments]     ${file1}        ${file2}
        ${contents1} =          Get File        ${file1}
        ${contents2} =          Get File        ${file2}
        Log To Console          ${contents1}
        Log To Console          ${contents2}
        Should Be Equal as Strings      ${contents1}    ${contents2}

#Smart Process   [Arguments] //TODO: variable number arguments keyword. logs to console and checks return code


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

TestIndexFASTA
#    double-check it's not currently in the database
#    ${search_result} =     Run Process     java    -jar    ${jar}    search	  ${newly_indexed_fasta}	 --db	 testdata/solr/
#    Should Not Contain         ${search_result.stdout}        gi|489223532|ref|WP_003131952.1|

    ${index_result} =     Run Process     java    -jar   ${jar}   index  ${newly_indexed_fasta}      --db    testdata/solr/
    Should Be Equal As Integers         ${index_result.rc}        0
    Log To Console       ${index_result.stdout}
    Log To Console        ${index_result.stderr}
    Should Be Equal As Integers         ${index_result.rc}        0

    ${search_result} =     Run Process     java    -jar    ${jar}    search	  ${newly_indexed_fasta}	 --db	 testdata/solr/

    Log To Console        ${search_result.stderr}
    Should Contain         ${search_result.stdout}        gi|489223532|ref|WP_003131952.1|

#TestIndexJSON
#    ${process_result} =     Run Process     java    -jar   ${jar}   index  testdata/data/toindex.fasta      --db    testdata/solr/      --is_dna


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
