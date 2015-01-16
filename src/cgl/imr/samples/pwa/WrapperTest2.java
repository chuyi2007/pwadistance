package cgl.imr.samples.pwa;


import edu.indiana.salsahpc.*;
import org.biojava3.alignment.template.SubstitutionMatrix;
import org.biojava3.core.sequence.ProteinSequence;
import org.biojava3.core.sequence.compound.AminoAcidCompound;

public class WrapperTest2 {
    public static void main(String[] args) {

        /*---------------------------------------------------------------------------*/
        // Testing CogAli Position Specific Matrix
        /*---------------------------------------------------------------------------*/

        // Creating protein sequences
        ProteinSequence cog0001 = new ProteinSequence("MSMSQQRPRSKQLFERAKQVFPGGVNSPVRAFKPYGGTPIFVERGEGAYIYDVDGNEYIDFCMGWGPLIFGHSHPEVVEAIQEQLERGLSFGAPTELEVELAELIVQRIPSVEKVRFVNSGTEATMSAIRLARGYTGRDKIIKFEGCYHGHHDSVLVKAGSGAATMGVPSSPGVPESVAKDTIVLPYNDIEALEEAFEEYGDDIAAVIVEPVMGNMGVIPPEEGFLEALRELCEEHGALLIFDEVMTGFRVGPGGAQEYYGIEPDLTTFGKIIGGGLPIGAFGGRREIMERIAPQGPVYQAGTFSGNPLAMAAGLATLEELQEEDLYEHLNQMAERLREGLEEVIEEHGIPMSVTRVGSMFSIFFTEEPVRNYEDAKKSDTEMFAKFFHELLNRGVYLPPSQFEAMFISTAHTDEDIDRTLEAIDEALKELAEKA");
        ProteinSequence cog0002 = new ProteinSequence("MMMKVGIVGASGYTGAELLRLLANHPEVEIVSITSRSYAGKPVSEVHPHLRGLVDLKFEDVDAEEIMNECDVVFLALPHGVSMELVPELLDSGVKVIDLSADFRLKDPEVYEKWYGFEHEAPDLLEEWVYGLPELHREEIRNAKLIANPGCYPTAAILALAPLVKEGLIDDSPVIVDAKSGVSGAGRKPSESNHFPEVNENLRPYGITSHRHTPEIEQELSRLSGKGVKVSFTPHLVPMTRGILATVYLHLKDGISEEEIHELYAEFYQNEPFVRVVPAGEYPDTKEVRGSNFCDIGIYVDEETNRVVVVSAIDNLVKGAAGQAVQNMNIMFGFDETTGLKYVPLYP");


        /*---------------------------------------------------------------------------*/
        // The usual way
        System.out.println("Without PS matrix");
        SubstitutionMatrix<AminoAcidCompound> matrix = MatrixUtil.getBlosum62();
        PerformAlignment(matrix, matrix, cog0001, cog0002);
        /*---------------------------------------------------------------------------*/


        /*---------------------------------------------------------------------------*/
        // With position specific matrix
        System.out.println("With PS matrix");
        String queryMatrixName = "COG0001"; // This is the name of the first sequence in CAPITAL
        String targetMatrixName = "COG0002"; // This is the name of the second sequence in CAPITAL
        String suffix = ".out.ascii"; // Default prefix. Use this as it is.
        try {
            // Now we have two scoring matrices. One for the first sequence and the other for the second
            SubstitutionMatrix<AminoAcidCompound> queryMatrix =
                    MatrixUtil.getAminoAcidCompoundPositionSpecificSubstitutionMatrix(queryMatrixName, suffix);
            SubstitutionMatrix<AminoAcidCompound> targetMatrix =
                    MatrixUtil.getAminoAcidCompoundPositionSpecificSubstitutionMatrix(targetMatrixName, suffix);

            PerformAlignment(queryMatrix, targetMatrix, cog0001, cog0002);
        } catch (MatrixNotFoundException e) {
            System.out.println("Couldn't find matrix");
        }
        /*---------------------------------------------------------------------------*/

    }

    private static void PerformAlignment(SubstitutionMatrix<AminoAcidCompound> queryMatrix, SubstitutionMatrix<AminoAcidCompound> targetMatrix, ProteinSequence seq1, ProteinSequence seq2) {
        AlignmentData ad = BioJavaWrapper.calculateAlignment(seq1, seq2, (short) 9, (short) 1, queryMatrix, targetMatrix, DistanceType.PercentIdentity);

        System.out.println("Score: " + ad.getScore());
        System.out.println("MaxScore: " + ad.getMaxScore());
        System.out.println("MinScore: " + ad.getMinScore());
        System.out.println("NormScore: " + ad.getNormalizedScore());
        System.out.println();
    }
}
